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

package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Base64;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;

public class JsonShareSerialiser implements ShareSerialiser {
  @Override
  @Nonnull
  public Single<String> serialise(@Nonnull final Share share) {
    checkNotNull(share, "\'share\' must not be null.");
    
    return Single.fromCallable(() -> {
      final String indexBase64Encoded = Base64.getEncoder().encodeToString(share.getIndex().toByteArray());
      final String valueBase64Encoded = Base64.getEncoder().encodeToString(share.getValue().toByteArray());
      
      final JsonObject json = new JsonObject();
      json.addProperty("index", indexBase64Encoded);
      json.addProperty("value", valueBase64Encoded);
      
      return json.toString();
    });
  }
  
  @Override
  @Nonnull
  public Single<Share> deserialise(@Nonnull final String serialisedShare) {
    checkNotNull(serialisedShare, "\'serialisedShare\' must not be null.");
    
    return Single
        .just(serialisedShare)
        .map(share -> {
          final JsonObject jsonObject = new JsonParser().parse(serialisedShare).getAsJsonObject();
          
          final byte[] indexBase64Decoded = Base64.getDecoder().decode(jsonObject.get("index").getAsString());
          final byte[] valueBase64Decoded = Base64.getDecoder().decode(jsonObject.get("value").getAsString());
          
          return Share
              .builder()
              .setIndex(new BigInteger(indexBase64Decoded))
              .setValue(new BigInteger(valueBase64Decoded))
              .build();
        })
        .onErrorResumeNext(error -> Single.error(
            new DeserialisationException(format("Cannot deserialise \'%1$s\'.", serialisedShare), error)));
  }
  
  @Override
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedShare) {
    checkNotNull(serialisedShare, "\'serialisedShare\' must not be null.");
    
    return deserialise(serialisedShare)
        .flatMap(result -> Single.just(true))
        .onErrorResumeNext(Single.just(false));
  }
}