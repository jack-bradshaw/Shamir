package com.matthewtamlin.shamir.commonslibrary.math;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkEachElementIsNotNull;
import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

/**
 * A polynomial with an arbitrary number of coefficients. The coefficients may be any number which can be expressed by a
 * {@link BigInteger}, however the exponents must all be non-negative integers.
 */
public class Polynomial {
    private final Map<Integer, BigInteger> coefficients;
    
    /**
     * Constructs a new Polynomial usign the provided map to define the coefficient for each exponent. For example, the
     * map {@code [0:3, 1:20, 2:-5, 4:100]} produces a polynomial equivalent to {@code 3 + 20x - 5x^2 + 100x^4}. Any
     * coefficient which is not defined defaults to zero.
     *
     * @param coefficients
     *         the exponent to coefficient mapping, not null, not containing null
     *
     * @throws IllegalArgumentException
     *         if {@code coefficients} contains a negative key
     */
    public Polynomial(@Nonnull final Map<Integer, BigInteger> coefficients) {
        checkNotNull(coefficients, "\'coefficients must not be null.");
        checkEachElementIsNotNull(coefficients.keySet(), "\'coefficients\' must not contain null keys.");
        checkEachElementIsNotNull(coefficients.values(), "\'coefficients\' must not contain null values.");
        
        for (final Integer index : coefficients.keySet()) {
            if (index < 0) {
                throw new IllegalArgumentException("\'coefficients\' must not contain negative indices.");
            }
        }
        
        this.coefficients = new HashMap<>(coefficients);
    }
    
    /**
     * Evaluates the polynomial at the supplied value.
     *
     * @param x
     *         the value to evaluate at, not null
     *
     * @return the resultant value, not null
     */
    @Nonnull
    public BigInteger evaluateAt(@Nonnull final BigInteger x) {
        checkNotNull(x, "\'x\' must not be null");
        
        BigInteger cumulativeValueAtX = BigInteger.ZERO;
        
        for (final Integer exponent : coefficients.keySet()) {
            final BigInteger coefficient = coefficients.get(exponent);
            final BigInteger xToPower = x.pow(exponent);
            
            cumulativeValueAtX = cumulativeValueAtX.add(coefficient.multiply(xToPower));
        }
        
        return cumulativeValueAtX;
    }
    
    /**
     * Evaluates the polynomial at the supplied value.
     *
     * @param x
     *         the value to evaluate at
     *
     * @return the resultant value, not null
     */
    @Nonnull
    public BigInteger evaluateAt(final int x) {
        return evaluateAt(BigInteger.valueOf(x));
    }
}