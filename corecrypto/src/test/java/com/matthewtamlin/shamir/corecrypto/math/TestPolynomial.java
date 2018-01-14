package com.matthewtamlin.shamir.corecrypto.math;

import com.google.common.collect.ImmutableMap;
import com.matthewtamlin.shamir.corecrypto.math.Polynomial;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for the {@link Polynomial} class.
 */
public class TestPolynomial {
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiation_nullCoefficients() {
        new Polynomial(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiation_coefficientsContainsNegativeIndex() {
        final Map<Integer, BigInteger> coefficients = ImmutableMap
                .<Integer, BigInteger>builder()
                .put(-1, BigInteger.ONE)
                .build();
        
        new Polynomial(coefficients);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiation_coefficientsContainsNullKey() {
        final Map<Integer, BigInteger> coefficients = new HashMap<>();
        
        coefficients.put(null, BigInteger.ONE);
        
        new Polynomial(coefficients);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiation_coefficientsContainsNullValue() {
        final Map<Integer, BigInteger> coefficients = new HashMap<>();
        
        coefficients.put(1, null);
        
        new Polynomial(coefficients);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEvaluateAt_null() {
        final Map<Integer, BigInteger> coefficients = ImmutableMap.of(0, BigInteger.valueOf(50));
        
        final Polynomial polynomial = new Polynomial(coefficients);
        
        polynomial.evaluateAt(null);
    }
    
    @Test
    public void testEvaluateAt_constantPolynomial() {
        final Map<Integer, BigInteger> coefficients = ImmutableMap.of(0, BigInteger.valueOf(50));
        
        final Polynomial polynomial = new Polynomial(coefficients);
        
        assertThat(polynomial.evaluateAt(-100), is(BigInteger.valueOf(50)));
        assertThat(polynomial.evaluateAt(0), is(BigInteger.valueOf(50)));
        assertThat(polynomial.evaluateAt(100), is(BigInteger.valueOf(50)));
    }
    
    @Test
    public void testEvaluateAt_linearPolynomial() {
        final Map<Integer, BigInteger> coefficients = ImmutableMap
                .<Integer, BigInteger>builder()
                .put(0, BigInteger.valueOf(50))
                .put(1, BigInteger.valueOf(100))
                .build();
        
        final Polynomial polynomial = new Polynomial(coefficients);
        
        assertThat(polynomial.evaluateAt(-100), is(BigInteger.valueOf(-9950)));
        assertThat(polynomial.evaluateAt(0), is(BigInteger.valueOf(50)));
        assertThat(polynomial.evaluateAt(100), is(BigInteger.valueOf(10050)));
    }
    
    @Test
    public void testEvaluateAt_quadraticPolynomial() {
        final Map<Integer, BigInteger> coefficients = ImmutableMap
                .<Integer, BigInteger>builder()
                .put(0, BigInteger.valueOf(50))
                .put(1, BigInteger.valueOf(100))
                .put(2, BigInteger.valueOf(-20))
                .build();
        
        final Polynomial polynomial = new Polynomial(coefficients);
        
        assertThat(polynomial.evaluateAt(-100), is(BigInteger.valueOf(-209950)));
        assertThat(polynomial.evaluateAt(0), is(BigInteger.valueOf(50)));
        assertThat(polynomial.evaluateAt(100), is(BigInteger.valueOf(-189950)));
    }
    
    @Test
    public void testEvaluateAt_higherOrderPolynomial() {
        final Polynomial polynomial = new Polynomial(ImmutableMap
                .<Integer, BigInteger>builder()
                .put(0, BigInteger.valueOf(50))
                .put(1, BigInteger.valueOf(100))
                .put(2, BigInteger.valueOf(-20))
                .put(3, BigInteger.valueOf(2856))
                .put(4, BigInteger.valueOf(195059))
                .put(5, BigInteger.valueOf(1))
                .put(8, BigInteger.valueOf(-70))
                .build());
        
        assertThat(polynomial.evaluateAt(-100), is(BigInteger.valueOf(-699980506956209950L)));
        assertThat(polynomial.evaluateAt(0), is(BigInteger.valueOf(50)));
        assertThat(polynomial.evaluateAt(100), is(BigInteger.valueOf(-699980481244189950L)));
    }
}