/*
 * Copyright 2018 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.shamir.commonslibrary.util;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

/**
 * A single key-value pair.
 *
 * @param <K>
 *     the type of the key
 * @param <V>
 *     the type of the value
 */
@AutoValue
public abstract class Pair<K, V> {
  /**
   * @return the key, not null
   */
  @Nonnull
  public abstract K getKey();
  
  /**
   * @return the value, not null
   */
  @Nonnull
  public abstract V getValue();
  
  /**
   * Constructs a new Pair.
   *
   * @param key
   *     the key, may be null
   * @param value
   *     the value, may be null
   * @param <K>
   *     the type of the key
   * @param <V>
   *     the type of the value
   *
   * @return the new Pair, not null
   */
  @Nonnull
  public static <K, V> Pair<K, V> create(@Nonnull K key, @Nonnull V value) {
    return new AutoValue_Pair<>(key, value);
  }
}