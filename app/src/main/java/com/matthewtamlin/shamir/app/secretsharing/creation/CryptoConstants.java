package com.matthewtamlin.shamir.app.secretsharing.creation;

import com.google.auto.value.AutoValue;
import com.matthewtamlin.shamir.app.secretsharing.creation.AutoValue_CryptoConstants;

import java.math.BigInteger;

@AutoValue
public abstract class CryptoConstants {
  public abstract BigInteger getPrime();
  
  public abstract int getMaxFileSizeBytes();
  
  public static Builder builder() {
    return new AutoValue_CryptoConstants.Builder();
  }
  
  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder setPrime(BigInteger prime);
    
    public abstract Builder setMaxFileSizeBytes(int maxFileSizeBytes);
    
    public abstract CryptoConstants build();
  }
}