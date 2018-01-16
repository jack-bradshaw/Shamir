package com.matthewtamlin.shamir.corecrypto.util;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A single key-value pair.
 *
 * @param <K>
 *         the type of the key
 * @param <V>
 *         the type of the value
 */
@AutoValue
public abstract class Pair<K, V> {
    /**
     * @return the key
     */
    @Nullable
    public abstract K getKey();
    
    /**
     * @return the value
     */
    @Nullable
    public abstract V getValue();
    
    /**
     * Constructs a new Pair.
     *
     * @param key
     *         the key, may be null
     * @param value
     *         the value, may be null
     * @param <K>
     *         the type of the key
     * @param <V>
     *         the type of the value
     *
     * @return the new Pair
     */
    @Nonnull
    public static <K, V> Pair<K, V> create(@Nullable K key, @Nullable V value) {
        return new AutoValue_Pair<>(key, value);
    }
}