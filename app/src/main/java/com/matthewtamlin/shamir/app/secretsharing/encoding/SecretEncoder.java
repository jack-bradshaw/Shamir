package com.matthewtamlin.shamir.app.secretsharing.encoding;

import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.math.BigInteger;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

public class SecretEncoder {
  @Nonnull
  public Single<BigInteger> encodeSecret(@Nonnull final byte[] secret) {
    checkNotNull(secret, "\'secret\' must not be null.");
    
    return Single.just(new BigInteger(secret));
  }
  
  @Nonnull
  public Single<byte[]> decodeSecret(@Nonnull final BigInteger encodedSecret) {
    checkNotNull(encodedSecret, "\'encodedSecret\' must not be null.");
    
    return Single.just(encodedSecret.toByteArray());
  }
}