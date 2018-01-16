package com.matthewtamlin.shamir.corecrypto.util;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the {@link Pair} class.
 */
public class TestPair {
    @Test
    public void testInstantiateAndGet_nullValues() {
        final Pair<String, Integer> pair = Pair.create(null, null);
        
        assertThat(pair.getKey(), is(nullValue()));
        assertThat(pair.getValue(), is(nullValue()));
    }
    
    @Test
    public void testInstantiateAndGet_nonNullValues() {
        final Pair<String, Integer> pair = Pair.create("test", 123456);
    
        assertThat(pair.getKey(), is("test"));
        assertThat(pair.getValue(), is(123456));
    }
}