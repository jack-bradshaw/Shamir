package com.matthewtamlin.shamir.corecrypto.crypto;

import com.matthewtamlin.shamir.corecrypto.math.Polynomial;
import com.matthewtamlin.shamir.corecrypto.model.CreationScheme;
import com.matthewtamlin.shamir.corecrypto.model.RecoveryScheme;
import com.matthewtamlin.shamir.corecrypto.model.Share;
import io.reactivex.Observable;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkEachElementIsNotNull;
import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Performs the share creation and secret recovery operations of Shamir's Secret Sharing. The operations are performed in
 * a finite field to prevent geometric attacks.
 */
public class Shamir {
    private final SecureRandom random;
    
    /**
     * Constructs a new Shamir. The provided random source is used in cryptographic operations, so ensure the source is
     * appropriately unpredictable.
     *
     * @param random
     *         the random source for use in cryptographic operations, not null
     */
    public Shamir(@Nonnull final SecureRandom random) {
        this.random = checkNotNull(random, "\'random\' must not be null.");
    }
    
    /**
     * Splits a secret into a set of shares using Shamir's Secret Sharing.
     *
     * @param secret
     *         the secret to share, not null
     * @param creationScheme
     *         defines the configuration to use creating the shares, not null
     *
     * @return the shares, not null, not containing null
     *
     * @throws IllegalArgumentException
     *         if the secret is greater than or equal to the prime defined in the creation scheme
     */
    @Nonnull
    public Set<Share> createShares(@Nonnull final BigInteger secret, @Nonnull final CreationScheme creationScheme) {
        checkNotNull(secret, "\'secret\' must not be null.");
        checkNotNull(creationScheme, "\'creationScheme\' must not be null.");
        
        checkArgument(
                creationScheme.getPrime().compareTo(secret) > 0,
                "\'secret\' must be less than the prime defined in \'creationScheme\'.");
        
        final Map<Integer, BigInteger> coefficients = Observable
                .range(1, creationScheme.getRequiredShareCount() - 1)
                .collectInto(
                        new HashMap<Integer, BigInteger>(),
                        (map, index) -> map.put(index, createRandomCoefficient(creationScheme.getPrime())))
                .blockingGet();
        
        coefficients.put(0, secret);
        
        final Polynomial polynomial = new Polynomial(coefficients);
        
        return Observable
                .range(1, creationScheme.getTotalShareCount())
                .collectInto(
                        new HashSet<Share>(),
                        (set, x) -> set.add(Share
                                .builder()
                                .setIndex(x)
                                .setValue(polynomial.evaluateAt(BigInteger.valueOf(x)).mod(creationScheme.getPrime()))
                                .build()))
                .blockingGet();
    }
    
    /**
     * Recovers a secret from a set of shares using Shamir's Secret Sharing.
     * <p>
     * This method operates within the limitations of Shamir's Secret Sharing. Given a set of shares and a recovery
     * scheme, it isn't possible to determine with any accuracy if the reconstructed secret is actually the original
     * secret. This can be of importance if the dealer is not trusted and must be managed externally.
     *
     * @param shares
     *         the shares to reconstruct the secret from, not null, not containing null
     * @param recoveryScheme
     *         defines the configuration to use then reconstructing the secret, not null
     *
     * @return the secret, not null
     *
     * @throws IllegalArgumentException
     *         if two or more shares have the same index
     * @throws IllegalArgumentException
     *         if the number of shares is less than the required share count defined in the recovery scheme
     * @throws IllegalArgumentException
     *         if the index of any share is greater than or equal to the prime defined in the recovery scheme
     * @throws IllegalArgumentException
     *         if the value of any share is greater than or equal to the prime defined in the recovery scheme
     */
    @Nonnull
    public BigInteger recoverSecret(@Nonnull final Set<Share> shares, @Nonnull final RecoveryScheme recoveryScheme) {
        checkNotNull(shares, "\'shares\' must not be null.");
        checkEachElementIsNotNull(shares, "\'shares\' must not contain null.");
        checkNotNull(recoveryScheme, "\'recoveryScheme\' must not be null.");
        
        validateShares(shares, recoveryScheme);
        
        final List<Share> orderedShares = new ArrayList<>(shares);
        orderedShares.sort(Comparator.comparing(Share::getIndex));
        
        BigInteger secret = BigInteger.ZERO;
        
        for (final Share outerShare : shares) {
            BigInteger outerWorking = ONE;
            
            for (final Share innerShare : shares) {
                if (outerShare.equals(innerShare)) {
                    continue;
                }
                
                final BigInteger innerWorking = innerShare
                        .getIndex()
                        .subtract(outerShare.getIndex())
                        .modInverse(recoveryScheme.getPrime());
                
                outerWorking = outerWorking.multiply(
                        innerShare
                                .getIndex()
                                .multiply(innerWorking));
            }
            
            outerWorking = outerWorking.multiply(outerShare.getValue());
            
            secret = secret.add(outerWorking);
        }
        
        secret = secret.mod(recoveryScheme.getPrime());
        
        return secret;
    }
    
    private void validateShares(final Set<Share> shares, final RecoveryScheme recoveryScheme) {
        if (shares.size() < recoveryScheme.getRequiredShareCount()) {
            throw new IllegalArgumentException(format(
                    "The recovery scheme requires at least %1$s shares, but only %2$s shares were provided.",
                    recoveryScheme.getRequiredShareCount(),
                    shares.size()));
        }
        
        final boolean allSharesHaveDistinctIndex = Observable
                .fromIterable(shares)
                .distinct(Share::getIndex)
                .count()
                .map(count -> count == shares.size())
                .blockingGet();
        
        if (!allSharesHaveDistinctIndex) {
            throw new IllegalArgumentException("Every share must have a distinct index.");
        }
        
        for (final Share s : shares) {
            if (s.getIndex().compareTo(recoveryScheme.getPrime()) >= 0) {
                throw new IllegalArgumentException("The index of every share must be less than the prime.");
            }
            
            if (s.getValue().compareTo(recoveryScheme.getPrime()) >= 0) {
                throw new IllegalArgumentException("The value of every share must be less than the prime.");
            }
        }
    }
    
    private BigInteger createRandomCoefficient(final BigInteger prime) {
        checkArgument(prime.compareTo(ONE) > 0, "\'prime\' must be greater than 1.");
        
        // Need to loop until the generated coefficient belongs to (0, limit)
        while (true) {
            final BigInteger randomCoefficient = new BigInteger(prime.bitLength(), random);
            
            if (randomCoefficient.compareTo(prime) >= 0) {
                continue;
            }
            
            if (randomCoefficient.compareTo(ZERO) == 0) {
                continue;
            }
            
            return randomCoefficient;
        }
    }
}