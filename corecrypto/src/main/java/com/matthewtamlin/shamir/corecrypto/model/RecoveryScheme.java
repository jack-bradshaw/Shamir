package com.matthewtamlin.shamir.corecrypto.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.sun.istack.internal.NotNull;

import javax.annotation.Nonnull;
import java.math.BigInteger;

import static java.lang.String.format;

/**
 * Defines the parameters to use when recovering a secret with Shamir's Secret Sharing. To successfully recover a secret,
 * the recovery scheme values must match the values used when creating the secret.
 */
@AutoValue
public abstract class RecoveryScheme {
    /**
     * @return the minimum number of shares needed to recover the secret
     */
    @SerializedName("requiredShareCount")
    public abstract int getRequiredShareCount();
    
    /**
     * @return the prime number which was used when sharing the secret
     */
    @SerializedName("prime")
    public abstract BigInteger getPrime();
    
    /**
     * @return a new {@link Builder}
     */
    @Nonnull
    public static Builder builder() {
        return new $AutoValue_RecoveryScheme.Builder();
    }
    
    /**
     * Provides a type adapter for serialising this class with Gson.
     *
     * @param gson
     *         a Gson instance, not null
     *
     * @return the type adapter factory, not null
     */
    @Nonnull
    public static TypeAdapter<RecoveryScheme> typeAdapter(@Nonnull final Gson gson) {
        return new AutoValue_RecoveryScheme.GsonTypeAdapter(gson);
    }
    
    /**
     * Builder class for the {@link RecoveryScheme} class.
     */
    @AutoValue.Builder
    public static abstract class Builder {
        /**
         * Sets the number of shares that are needed to recovery the secret. The value must: <ul><li>Be greater than
         * 1.</li><li>Be less than the prime.</li></ul>
         * <p>
         * To successfully recover a secret, this value must match the value used when creating the secret.
         *
         * @param requiredShareCount
         *         the required share count
         *
         * @return this builder, not null
         */
        public abstract Builder setRequiredShareCount(int requiredShareCount);
        
        /**
         * Sets the prime number to use when recovering the shares. The prime number is used to construct a finite field
         * and prevent geometric attacks. The value must: <ul><li>Not be null.</li><li>Be greater than 1.</li><li>Be
         * greater than the total share count.</li><li>Be greater than the secret it is being used to recover.</li></ul>
         * <p>
         * To successfully recover a secret, this value must match the value used when creating the secret.
         *
         * @param prime
         *         the prime to use
         *
         * @return this builder, not null
         */
        public abstract Builder setPrime(BigInteger prime);
        
        abstract RecoveryScheme autoBuild();
        
        /**
         * Sets the prime number to use when recovering the shares. The prime number is used to construct a finite field
         * and prevent geometric attacks. The value must: <ul><li>Be greater than 1.</li><li>Be greater than the total
         * share count.</li><li>Be greater than the secret it is being used to recover.</li></ul>
         * <p>
         * To successfully recover a secret, this value must equal the value used when creating the secret.
         *
         * @param prime
         *         the prime to use
         *
         * @return this builder, not null
         */
        @Nonnull
        public Builder setPrime(final int prime) {
            return setPrime(BigInteger.valueOf(prime));
        }
        
        /**
         * Constructs a {@link RecoveryScheme} based on this builder. This method will fail if any of the properties were
         * never set or were set to invalid values (see the documentation for each method for specifics).
         *
         * @return an immutable RecoveryScheme based on this builder, not null
         *
         * @throws IllegalStateException
         *         if any of the values are missing or invalid
         */
        @NotNull
        public RecoveryScheme build() {
            final RecoveryScheme recoveryScheme = autoBuild();
            
            if (recoveryScheme.getRequiredShareCount() < 2) {
                throw new IllegalStateException(format(
                        "The required share count must be at least 2. Current value is %1$s.",
                        recoveryScheme.getRequiredShareCount()));
            }
            
            if (recoveryScheme.getPrime().compareTo(BigInteger.valueOf(recoveryScheme.getRequiredShareCount())) <= 0) {
                throw new IllegalStateException(format(
                        "The prime must be greater than the required share count." +
                                "Current values are %1$s and %2$s respectively.",
                        recoveryScheme.getPrime(),
                        recoveryScheme.getRequiredShareCount()));
            }
            
            return recoveryScheme;
        }
    }
}