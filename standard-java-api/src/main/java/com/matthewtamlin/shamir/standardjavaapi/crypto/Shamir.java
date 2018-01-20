package com.matthewtamlin.shamir.standardjavaapi.crypto;

import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import io.reactivex.Observable;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

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
    private final RxShamir rxShamir;
    
    /**
     * Constructs a new Shamir. The provided random source is used in cryptographic operations, so ensure the source is
     * appropriately unpredictable.
     *
     * @param random
     *         the random source for use in cryptographic operations, not null
     */
    public Shamir(@Nonnull final SecureRandom random) {
        checkNotNull(random, "\'random\' must not be null.");
        
        rxShamir = RxShamir.create(random);
    }
    
    /**
     * Constructs a new Shamir.
     * <p>
     * The {@link #Shamir(SecureRandom)} constructor is provided as an alternative to this method.
     *
     * @param random
     *         the random source for use in cryptographic operations, not null
     */
    @Nonnull
    public static Shamir create(@Nonnull final SecureRandom random) {
        return new Shamir(random);
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
        return rxShamir
                .createShares(secret, creationScheme)
                .collectInto(new HashSet<Share>(), Set::add)
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
        return rxShamir.recoverSecret(shares, recoveryScheme).blockingGet();
    }
}