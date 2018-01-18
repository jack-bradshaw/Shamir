package com.matthewtamlin.shamir.corecrypto.model;

import com.matthewtamlin.shamir.corecommons.model.RecoveryScheme;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the {@link RecoveryScheme} class.
 */
public class TestRecoveryScheme {
    private static final BigInteger SEVEN = BigInteger.valueOf(7);
    
    @Test(expected = IllegalStateException.class)
    public void testInstantiation_requiredShareCountNeverSet() {
        RecoveryScheme
                .builder()
                .setPrime(7)
                .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInstantiation_primeNeverSet() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(2)
                .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInstantiation_requiredShareCountLessThan2() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(1)
                .setPrime(7)
                .build();
    }
    
    @Test
    public void testInstantiation_requiredShareCountEqualTo2() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(2)
                .setPrime(7)
                .build();
    }
    
    @Test
    public void testInstantiation_requiredShareCountGreaterThan2() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(3)
                .setPrime(7)
                .build();
    }
    
    @Test
    public void testInstantiate_requiredShareCountLessThanPrime() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(2)
                .setPrime(7)
                .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInstantiation_requiredShareCountEqualToPrime() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(7)
                .setPrime(7)
                .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInstantiation_requiredShareCountGreaterThanPrime() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(8)
                .setPrime(7)
                .build();
    }
    
    @Test(expected = NullPointerException.class)
    public void testInstantiation_nullPrime() {
        RecoveryScheme
                .builder()
                .setRequiredShareCount(2)
                .setPrime(null)
                .build();
    }
    
    @Test
    public void testInstantiateAndGet_builtUsingIntegerPrime() {
        final RecoveryScheme recoveryScheme = RecoveryScheme
                .builder()
                .setRequiredShareCount(2)
                .setPrime(7)
                .build();
        
        assertThat(recoveryScheme.getRequiredShareCount(), is(2));
        assertThat(recoveryScheme.getPrime(), is(SEVEN));
    }
    
    @Test
    public void testInstantiateAndGet_builtUsingBigIntegerPrime() {
        final RecoveryScheme recoveryScheme = RecoveryScheme
                .builder()
                .setRequiredShareCount(2)
                .setPrime(SEVEN)
                .build();
        
        assertThat(recoveryScheme.getRequiredShareCount(), is(2));
        assertThat(recoveryScheme.getPrime(), is(SEVEN));
    }
}