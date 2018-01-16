package com.matthewtamlin.shamir.corecrypto.util;

import com.google.auto.value.AutoValue;
import com.sun.istack.internal.NotNull;

import javax.annotation.Nonnull;

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
     * @return the key, not null
     */
    @NotNull
    public abstract K getKey();
    
    /**
     * @return the value, not null
     */
    @NotNull
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
    public static <K, V> Pair<K, V> create(@NotNull K key, @NotNull V value) {
        return new AutoValue_Pair<>(key, value);
    }
}