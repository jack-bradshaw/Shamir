package com.matthewtamlin.shamir.commonslibrary.model;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the {@link CreationScheme} class.
 */
public class TestCreationScheme {
  private static final BigInteger SEVEN = BigInteger.valueOf(7);
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_requiredShareCountNeverSet() {
    CreationScheme
        .builder()
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_totalShareCountNeverSet() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_primeNeverSet() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(2)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_requiredShareCountLessThan2() {
    CreationScheme
        .builder()
        .setRequiredShareCount(1)
        .setTotalShareCount(2)
        .setPrime(7)
        .build();
  }
  
  @Test
  public void testInstantiation_requiredShareCountEqualTo2() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(2)
        .setPrime(7)
        .build();
  }
  
  @Test
  public void testInstantiation_requiredShareCountGreaterThan2() {
    CreationScheme
        .builder()
        .setRequiredShareCount(3)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_primeLessThanTotalShareCount() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(4)
        .setPrime(3)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_primeEqualToTotalShareCount() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(3)
        .build();
  }
  
  @Test
  public void testInstantiation_primeGreaterThanTotalShareCount() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(4)
        .setPrime(5)
        .build();
  }
  
  @Test(expected = NullPointerException.class)
  public void testInstantiation_nullPrime() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(null)
        .build();
  }
  
  @Test
  public void testInstantiation_requiredShareCountLessThanTotalShareCount() {
    CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
  }
  
  @Test
  public void testInstantiation_requiredShareCountEqualToTotalShareCount() {
    CreationScheme
        .builder()
        .setRequiredShareCount(3)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_requiredShareCountGreaterThanTotalShareCount() {
    CreationScheme
        .builder()
        .setRequiredShareCount(4)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
  }
  
  @Test
  public void testInstantiateThenGet_builtUsingIntegerPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
    
    assertThat(creationScheme.getRequiredShareCount(), is(2));
    assertThat(creationScheme.getTotalShareCount(), is(3));
    assertThat(creationScheme.getPrime(), is(SEVEN));
  }
  
  @Test
  public void testInstantiateThenGet_builtUsingBigIntegerPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(SEVEN)
        .build();
    
    assertThat(creationScheme.getRequiredShareCount(), is(2));
    assertThat(creationScheme.getTotalShareCount(), is(3));
    assertThat(creationScheme.getPrime(), is(SEVEN));
  }
}