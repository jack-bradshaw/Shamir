package com.matthewtamlin.shamir.corecrypto.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the {@link Pair} class.
 */
public class TestPair {
    @Test(expected = NullPointerException.class)
    public void testInstantiate_nullKey() {
        Pair.create(null, 123456);
    }
    
    @Test(expected = NullPointerException.class)
    public void testInstantiate_nullValue() {
        Pair.create("test", null);
    }
    
    @Test
    public void testInstantiateAndGet() {
        final Pair<String, Integer> pair = Pair.create("test", 123456);
        
        assertThat(pair.getKey(), is("test"));
        assertThat(pair.getValue(), is(123456));
    }
}