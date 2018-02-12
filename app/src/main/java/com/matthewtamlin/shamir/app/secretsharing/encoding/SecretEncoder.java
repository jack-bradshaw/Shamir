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

package com.matthewtamlin.shamir.app.secretsharing.encoding;

import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.math.BigInteger;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

public class SecretEncoder {
  @Nonnull
  public Single<BigInteger> encodeSecret(@Nonnull final byte[] secret) {
    checkNotNull(secret, "\'secret\' must not be null.");
    
    return Single.fromCallable(() -> {
      final byte[] secretWithLeading1 = new byte[secret.length + 1];
      
      secretWithLeading1[0] = 1;
      
      System.arraycopy(secret, 0, secretWithLeading1, 1, secret.length);
      
      return new BigInteger(secretWithLeading1);
    });
  }
  
  @Nonnull
  public Single<byte[]> decodeSecret(@Nonnull final BigInteger encodedSecret) {
    checkNotNull(encodedSecret, "\'encodedSecret\' must not be null.");
    
    return Single.fromCallable(() -> {
      final byte[] secretWithLeading1 = encodedSecret.toByteArray();
      final byte[] secretWithoutLeading1 = new byte[secretWithLeading1.length - 1];
      
      System.arraycopy(secretWithLeading1, 1, secretWithoutLeading1, 0, secretWithoutLeading1.length);
      
      return secretWithoutLeading1;
    });
  }
}