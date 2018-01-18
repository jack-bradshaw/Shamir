package com.matthewtamlin.shamir.rxcorecrypto;

import com.google.common.collect.ImmutableSet;
import com.matthewtamlin.shamir.corecrypto.crypto.Shamir;
import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link RxShamir} class.
 */
public class TestRxShamir {
    private static final CreationScheme CREATION_SCHEME = CreationScheme
            .builder()
            .setRequiredShareCount(2)
            .setTotalShareCount(3)
            .setPrime(7)
            .build();
    
    private static final RecoveryScheme RECOVERY_SCHEME = RecoveryScheme
            .builder()
            .setRequiredShareCount(2)
            .setPrime(7)
            .build();
    
    private Shamir shamir;
    
    private RxShamir rxShamir;
    
    @Before
    public void setup() {
        shamir = mock(Shamir.class);
        rxShamir = RxShamir.from(shamir);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiation_nullShamir() {
        RxShamir.from(null);
    }
    
    @Test
    public void testCreateShares_completesSuccessfully() {
        final Set<Share> shares = ImmutableSet
                .<Share>builder()
                .add(Share.builder().setIndex(1).setValue(1).build())
                .add(Share.builder().setIndex(2).setValue(2).build())
                .build();
        
        when(shamir.createShares(any(), any())).thenReturn(shares);
        
        rxShamir.createShares(ONE, CREATION_SCHEME)
                .test()
                .awaitDone(200, MILLISECONDS)
                .assertNoErrors()
                .assertValue(shares);
    }
    
    @Test
    public void testCreateShares_completesWithError() throws InterruptedException {
        final RuntimeException error = new RuntimeException();
        
        when(shamir.createShares(any(), any())).thenThrow(error);
        
        rxShamir.createShares(ONE, CREATION_SCHEME)
                .test()
                .awaitDone(200, MILLISECONDS)
                .assertError(error)
                .assertNoValues();
    }
    
    @Test
    public void testRecoverSecret_completesSuccessfully() {
        final BigInteger secret = TEN;
        
        when(shamir.recoverSecret(any(), any())).thenReturn(secret);
        
        rxShamir.recoverSecret(new HashSet<>(), RECOVERY_SCHEME)
                .test()
                .awaitDone(200, MILLISECONDS)
                .assertNoErrors()
                .assertValue(secret);
    }
    
    @Test
    public void testRecoverSecret_completesWithError() {
        final RuntimeException error = new RuntimeException();
        
        when(shamir.recoverSecret(any(), any())).thenThrow(error);
        
        rxShamir.recoverSecret(new HashSet<>(), RECOVERY_SCHEME)
                .test()
                .awaitDone(200, MILLISECONDS)
                .assertError(error)
                .assertNoValues();
    }
}