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

package com.matthewtamlin.shamir.commonslibrary.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * A share produced by Shamir's Secret Sharing.
 * <p>
 * A Share consists of an index and a value. In the context of Shamir's secret sharing, the index and the value
 * correspond to the point (index, value) in a finite field.
 */
@AutoValue
public abstract class Share {
  /**
   * @return the index of the share, not null
   */
  @SerializedName("index")
  public abstract BigInteger getIndex();
  
  /**
   * @return the value of the share, not null
   */
  @SerializedName("value")
  public abstract BigInteger getValue();
  
  /**
   * @return a new {@link Builder}
   */
  @Nonnull
  public static Builder builder() {
    return new $AutoValue_Share.Builder();
  }
  
  /**
   * Creates a type adapter for serialising this class with Gson.
   *
   * @param gson
   *     a Gson instance, not null
   *
   * @return the type adapter, not null
   */
  @Nonnull
  public static TypeAdapter<Share> typeAdapter(@Nonnull final Gson gson) {
    return new AutoValue_Share.GsonTypeAdapter(gson);
  }
  
  /**
   * Builder class for the {@link Share} class.
   */
  @AutoValue.Builder
  public static abstract class Builder {
    /**
     * Sets the index of this share. The index must be: <ul><li>Not null.</li><li>Greater than or equal to 1.</li></ul>
     *
     * @param index
     *     the index, not null
     *
     * @return this builder, not null
     */
    public abstract Builder setIndex(BigInteger index);
    
    /**
     * Sets the value of this share. The value must not be null.
     *
     * @param value
     *     the value, not null
     *
     * @return this builder, not null
     */
    public abstract Builder setValue(BigInteger value);
    
    abstract Share autoBuild();
    
    /**
     * Sets the index of this share. The index must be greater than or equal to 1.
     *
     * @param index
     *     the index
     *
     * @return this builder, not null
     */
    @Nonnull
    public Builder setIndex(final long index) {
      return setIndex(BigInteger.valueOf(index));
    }
    
    /**
     * Sets the value of this share.
     *
     * @param value
     *     the value
     *
     * @return this builder, not null
     */
    @Nonnull
    public Builder setValue(final long value) {
      return setValue(BigInteger.valueOf(value));
    }
    
    /**
     * Constructs a {@link Share} based on this builder. This method will fail if any of the properties were never
     * set or were set to invalid values (see the documentation of each method for specifics).
     *
     * @return a Share based on this builder, not null
     *
     * @throws IllegalStateException
     *     if any of the values are missing or invalid
     */
    @Nonnull
    public Share build() {
      final Share share = autoBuild();
      
      if (share.getIndex().compareTo(BigInteger.ONE) < 0) {
        throw new IllegalStateException("The index must be at least 1.");
      }
      
      return share;
    }
  }
}