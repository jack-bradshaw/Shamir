package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Base64;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;

public class JsonRecoverySchemeSerialiser implements RecoverySchemeSerialiser {
  @Override
  @Nonnull
  public Single<String> serialise(@Nonnull final RecoveryScheme scheme) {
    checkNotNull(scheme, "\'scheme\' must not be null.");
    
    return Single.fromCallable(() -> {
      final String primeBase64Encoded = Base64.getEncoder().encodeToString(scheme.getPrime().toByteArray());
      
      final JsonObject json = new JsonObject();
      json.addProperty("requiredShareCount", scheme.getRequiredShareCount());
      json.addProperty("prime", primeBase64Encoded);
      
      return json.toString();
    });
  }
  
  @Override
  @Nonnull
  public Single<RecoveryScheme> deserialise(@Nonnull final String serialisedScheme) {
    checkNotNull(serialisedScheme, "\'serialisedScheme\' must not be null.");
    
    return Single
        .just(serialisedScheme)
        .map(scheme -> {
          final JsonObject jsonObject = new JsonParser().parse(serialisedScheme).getAsJsonObject();
          
          final byte[] primeBase64Decoded = Base64.getDecoder().decode(jsonObject.get("prime").getAsString());
          
          return RecoveryScheme
              .builder()
              .setRequiredShareCount(jsonObject.get("requiredShareCount").getAsInt())
              .setPrime(new BigInteger(primeBase64Decoded))
              .build();
        })
        .onErrorResumeNext(error -> Single.error(
            new DeserialisationException(format("Cannot deserialise \'%1$s.\'", serialisedScheme), error)));
  }
  
  @Override
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedScheme) {
    checkNotNull(serialisedScheme, "\'serialisedScheme\' must not be null.");
    
    return deserialise(serialisedScheme)
        .flatMap(result -> Single.just(true))
        .onErrorResumeNext(Single.just(false));
  }
}