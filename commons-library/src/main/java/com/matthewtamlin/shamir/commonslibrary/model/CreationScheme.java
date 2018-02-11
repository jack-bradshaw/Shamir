package com.matthewtamlin.shamir.commonslibrary.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nonnull;
import java.math.BigInteger;

import static java.lang.String.format;

/**
 * Defines the parameters to use when sharing a secret with Shamir's Secret Sharing.
 */
@AutoValue
public abstract class CreationScheme {
  /**
   * @return the minimum number of shares needed to recover the secret
   */
  @SerializedName("requiredShareCount")
  public abstract int getRequiredShareCount();
  
  /**
   * @return the total number of shares to create
   */
  @SerializedName("totalShareCount")
  public abstract int getTotalShareCount();
  
  /**
   * @return the prime number to use as the basis of the finite field, not null
   */
  @SerializedName("prime")
  public abstract BigInteger getPrime();
  
  /**
   * @return a new {@link Builder}
   */
  @Nonnull
  public static Builder builder() {
    return new AutoValue_CreationScheme.Builder();
  }
  
  /**
   * Creates a type adapter for serialising this class with Gson.
   *
   * @param gson
   *     a Gson instance
   *
   * @return the type adapter, not null
   */
  @Nonnull
  public static TypeAdapter<CreationScheme> typeAdapter(@Nonnull final Gson gson) {
    return new AutoValue_CreationScheme.GsonTypeAdapter(gson);
  }
  
  /**
   * Builder class for the {@link CreationScheme} class.
   */
  @AutoValue.Builder
  public static abstract class Builder {
    /**
     * Sets the number of shares that are needed to recover the secret. The value must be: <ul><li>Greater than
     * 1.</li><li>Less than or equal to the total share count.</li><li>Less than the prime.</li></ul>
     *
     * @param count
     *     the minimum number of shares needed to recover the secret
     *
     * @return this builder, not null
     */
    public abstract Builder setRequiredShareCount(int count);
    
    /**
     * Sets the total number of shares to create. The value must be: <ul><li>Greater than 1.</li><li>Greater than or
     * equal to the required share count.</li><li>Less than the prime.</li></ul>
     *
     * @param count
     *     the total number of shares to create
     *
     * @return this builder, not null
     */
    public abstract Builder setTotalShareCount(int count);
    
    /**
     * Sets the prime number to use as the basis of the finite field. The value must be: <ul><li>Not
     * null.</li><li>Greater than 1.</li><li>Greater than the required share count.</li><li>Greater than the total
     * share count.</li></ul>
     *
     * @param prime
     *     the prime to use, not null
     *
     * @return this builder, not null
     */
    public abstract Builder setPrime(BigInteger prime);
    
    abstract CreationScheme autoBuild();
    
    /**
     * Sets the prime number to use as the basis of the finite field. The value must be: <ul><li>Not
     * null.</li><li>Greater than 1.</li><li>Greater than the required share count.</li><li>Greater than the total
     * share count.</li></ul>
     *
     * @param prime
     *     the prime to use
     *
     * @return this builder, not null
     */
    @Nonnull
    public Builder setPrime(final int prime) {
      return setPrime(BigInteger.valueOf(prime));
    }
    
    /**
     * Constructs a {@link CreationScheme} based on this builder. This method will fail if any of the properties were
     * never set or were set to invalid values (see the documentation of each method for specifics).
     *
     * @return a CreationScheme based on this builder, not null
     *
     * @throws IllegalStateException
     *     if any of the values are missing or invalid
     */
    @Nonnull
    public CreationScheme build() {
      final CreationScheme creationScheme = autoBuild();
      
      if (creationScheme.getRequiredShareCount() <= 1) {
        throw new IllegalStateException(format(
            "The required share count must be at least 2. Current value is %1$s.",
            creationScheme.getRequiredShareCount()));
      }
      
      if (creationScheme.getRequiredShareCount() > creationScheme.getTotalShareCount()) {
        throw new IllegalStateException(format(
            "The required share count must not exceed the total share count. " +
                "Current values are %1$s and %2$s respectively.",
            creationScheme.getRequiredShareCount(),
            creationScheme.getTotalShareCount()));
      }
      
      if (creationScheme.getPrime().compareTo(BigInteger.valueOf(creationScheme.getTotalShareCount())) <= 0) {
        throw new IllegalStateException(format(
            "The prime must be greater than the total share count. " +
                "Current values are %1$s and %2$s respectively.",
            creationScheme.getPrime(),
            creationScheme.getTotalShareCount()));
      }
      
      return creationScheme;
    }
  }
}