package com.matthewtamlin.shamir.reactivejavaapi;

import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.standardjavaapi.crypto.Shamir;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Set;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;


/**
 * Wraps an instance of {@link Shamir} to provide reactive-style access to its methods. To instantiate the class, use the
 * {@link #from(Shamir)} method.
 */
public class RxShamir {
    private Shamir shamir;
    
    private RxShamir(final Shamir shamir) {
        this.shamir = checkNotNull(shamir, "\'shamir\' must not be null.");
    }
    
    /**
     * Creates a Single which calls the {@link Shamir#createShares(BigInteger, CreationScheme)} method of the wrapped
     * Shamir instance and emits the resulting shares. Any throwables thrown by the method are emitted by the single as
     * errors.
     * <p>
     * The returned single does not operate by default on a particular scheduler.
     *
     * @param secret
     *         the secret to share, not null
     * @param creationScheme
     *         defines the configuration to use creating the shares, not null
     *
     * @return the single, not null
     */
    @Nonnull
    public Single<Set<Share>> createShares(
            @Nonnull final BigInteger secret,
            @Nonnull final CreationScheme creationScheme) {
        
        return Single.fromCallable(() -> shamir.createShares(secret, creationScheme));
    }
    
    /**
     * Creates a Single which calls the {@link Shamir#recoverSecret(Set, RecoveryScheme)} method of the wrapped Shamir
     * instance and emits the resulting secret. Any throwables thrown by the method are emitted by the single as errors.
     * <p>
     * The returned single does not operate by default on a particular scheduler.
     *
     * @param shares
     *         the shares to reconstruct the secret from, not null, not containing null
     * @param recoveryScheme
     *         defines the configuration to use then reconstructing the secret, not null
     *
     * @return the single, not null
     */
    @Nonnull
    public Single<BigInteger> recoverSecret(
            @Nonnull final Set<Share> shares,
            @Nonnull final RecoveryScheme recoveryScheme) {
        
        return Single.fromCallable(() -> shamir.recoverSecret(shares, recoveryScheme));
    }
    
    /**
     * Wraps the provided Shamir instance to provide reactive-style access to its methods.
     *
     * @param shamir
     *         the instance to wrap, not null
     *
     * @return the wrapper, not null
     */
    @Nonnull
    public static RxShamir from(@Nonnull final Shamir shamir) {
        return new RxShamir(shamir);
    }
}