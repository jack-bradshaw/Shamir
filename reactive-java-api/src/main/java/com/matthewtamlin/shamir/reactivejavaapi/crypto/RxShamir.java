package com.matthewtamlin.shamir.reactivejavaapi.crypto;

import com.matthewtamlin.shamir.commonslibrary.math.Polynomial;
import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.commonslibrary.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Set;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkEachElementIsNotNull;
import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Performs the share creation and secret recovery operations of Shamir's Secret Sharing. All cryptographic operations
 * are performed in a finite field to prevent geometric attacks.
 * <p>
 * For convenience, this class can be instantiated using either the {@link #RxShamir(SecureRandom)} constructor or the
 * {@link #create(SecureRandom)} static method.
 */
public class RxShamir {
    private final SecureRandom random;
    
    /**
     * Constructs a new RxShamir.
     * <p>
     * The {@link #create(SecureRandom)} static method is provided as an alternative to this constructor.
     *
     * @param random
     *         the random source for use in cryptographic operations, not null
     */
    public RxShamir(@Nonnull final SecureRandom random) {
        this.random = checkNotNull(random, "\'random\' must not be null.");
    }
    
    /**
     * Constructs a new RxShamir.
     * <p>
     * The {@link #RxShamir(SecureRandom)} constructor is provided as an alternative to this method.
     *
     * @param random
     *         the random source for use in cryptographic operations, not null
     */
    @Nonnull
    public static RxShamir create(@Nonnull final SecureRandom random) {
        return new RxShamir(random);
    }
    
    /**
     * Splits a secret into a set of shares using Shamir's Secret Sharing.
     * <p>
     * The operation will fail with an {@link IllegalStateException} if the secret is not less than the prime specified
     * in the creation scheme.
     * <p>
     * The returned observable does not operate on a particular scheduler by default.
     *
     * @param secret
     *         the secret to share, not null
     * @param creationScheme
     *         defines the sharing configuration, not null
     *
     * @return an observable which emits the shares then completes, not null
     */
    @Nonnull
    public Observable<Share> createShares(
            @Nonnull final BigInteger secret,
            @Nonnull final CreationScheme creationScheme) {
        
        checkNotNull(secret, "\'secret\' must not be null.");
        checkNotNull(creationScheme, "\'creationScheme\' must not be null.");
        
        final Single<Polynomial> polynomial = Observable
                .range(1, creationScheme.getRequiredShareCount() - 1)
                .flatMapSingle(index -> createRandomCoefficient(creationScheme.getPrime())
                        .map(coefficient -> Pair.create(index, coefficient)))
                .startWith(Pair.create(0, secret))
                .collectInto(new HashMap<Integer, BigInteger>(), (map, pair) -> map.put(pair.getKey(), pair.getValue()))
                .map(Polynomial::new);
        
        final Observable<Share> shares = polynomial
                .flatMapObservable(polynomialVal -> Observable
                        .range(1, creationScheme.getTotalShareCount())
                        .map(index -> Pair.create(
                                index,
                                polynomialVal
                                        .evaluateAt(BigInteger.valueOf(index))
                                        .mod(creationScheme.getPrime())))
                        .map(pair -> Share
                                .builder()
                                .setIndex(pair.getKey())
                                .setValue(pair.getValue())
                                .build()));
        
        return checkCreationInformation(secret, creationScheme).andThen(shares);
    }
    
    /**
     * Recovers a secret from a set of shares using Shamir's Secret Sharing.
     * <p>
     * The operation will fail with an {@link IllegalStateException} if: <ul><li>The shares do not have mutually distinct
     * indices.</li> <li>The number of shares is less than the required share count specified in the recovery
     * scheme.</li> <li>The index of any share is greater than or equal to the prime specified in the recovery
     * scheme.</li> <li>The value of any share is greater than or equal to the prime specified in the recovery
     * scheme.</li></ul>
     *
     * @param shares
     *         the shares to reconstruct the secret from, not null, not containing null
     * @param recoveryScheme
     *         defines the recovery configuration, not null
     *
     * @return a single which emits the recovered secret, not null
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
    public Single<BigInteger> recoverSecret(
            @Nonnull final Set<Share> shares,
            @Nonnull final RecoveryScheme recoveryScheme) {
        
        checkNotNull(shares, "\'shares\' must not be null.");
        checkEachElementIsNotNull(shares, "\'shares\' must not contain null.");
        checkNotNull(recoveryScheme, "\'recoveryScheme\' must not be null.");
        
        final Single<BigInteger> secret = Observable
                .fromIterable(shares)
                .flatMapSingle(outerShare -> Observable
                        .fromIterable(shares)
                        .filter(innerShare -> !innerShare.equals(outerShare))
                        .map(innerShare -> {
                            final BigInteger indexDifferenceModInverse = innerShare
                                    .getIndex()
                                    .subtract(outerShare.getIndex())
                                    .modInverse(recoveryScheme.getPrime());
                            
                            return innerShare
                                    .getIndex()
                                    .multiply(indexDifferenceModInverse);
                        })
                        .reduce(ONE, BigInteger::multiply)
                        .map(value -> value.multiply(outerShare.getValue())))
                .reduce(ZERO, BigInteger::add)
                .map(workingSecret -> workingSecret.mod(recoveryScheme.getPrime()));
        
        return checkRecoveryInformation(shares, recoveryScheme).andThen(secret);
    }
    
    private Completable checkCreationInformation(final BigInteger secret, final CreationScheme creationScheme) {
        return Completable.create(emitter -> {
            if (creationScheme.getPrime().compareTo(secret) <= 0) {
                emitter.onError(new IllegalStateException("The secret must be less than the prime."));
                
            } else {
                emitter.onComplete();
            }
        });
    }
    
    private Single<BigInteger> createRandomCoefficient(final BigInteger prime) {
        return Observable
                .fromCallable(() -> new BigInteger(prime.bitLength(), random))
                .filter(randomNumber -> randomNumber.compareTo(ZERO) > 0)
                .filter(randomNumber -> randomNumber.compareTo(prime) < 0)
                .firstOrError()
                .retry();
    }
    
    private Completable checkRecoveryInformation(final Set<Share> shares, final RecoveryScheme recoveryScheme) {
        final Completable checkShareCountSatisfiedRequiredShareCount = Single
                .fromCallable(shares::size)
                .map(shareCount -> shareCount >= recoveryScheme.getRequiredShareCount())
                .flatMapCompletable(shareCountIsValid -> shareCountIsValid ?
                        Completable.complete() :
                        Completable.error(new IllegalStateException(format(
                                "The recovery scheme requires at least %1$s shares, but only %2$s shares were provided.",
                                recoveryScheme.getRequiredShareCount(),
                                shares.size()))));
        
        
        final Completable checkIndicesAreDistinct = Observable
                .fromIterable(shares)
                .distinct(Share::getIndex)
                .count()
                .map(count -> count == shares.size())
                .flatMapCompletable(allIndicesAreDistinct -> allIndicesAreDistinct ?
                        Completable.complete() :
                        Completable.error(new IllegalStateException("Every share must have a distinct index.")));
        
        final Completable checkIndicesAreLessThanPrime = Observable
                .fromIterable(shares)
                .map(share -> share.getIndex().compareTo(recoveryScheme.getPrime()) < 0)
                .flatMapCompletable(indexIsValid -> indexIsValid ?
                        Completable.complete() :
                        Completable.error(
                                new IllegalStateException("The index of every share must be less than the prime.")));
        
        final Completable checkValuesAreLessThanPrime = Observable
                .fromIterable(shares)
                .map(share -> share.getValue().compareTo(recoveryScheme.getPrime()) < 0)
                .flatMapCompletable(indexIsValid -> indexIsValid ?
                        Completable.complete() :
                        Completable.error(
                                new IllegalStateException("The index of every share must be less than the prime.")));
        
        return checkShareCountSatisfiedRequiredShareCount
                .andThen(checkIndicesAreDistinct)
                .andThen(checkIndicesAreLessThanPrime)
                .andThen(checkValuesAreLessThanPrime);
    }
}