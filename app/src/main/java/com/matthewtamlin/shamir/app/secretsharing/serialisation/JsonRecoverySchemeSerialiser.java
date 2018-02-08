package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.google.gson.Gson;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import io.reactivex.Single;

import javax.annotation.Nonnull;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

public class JsonRecoverySchemeSerialiser implements RecoverySchemeSerialiser {
  private Gson gson;
  
  public JsonRecoverySchemeSerialiser(@Nonnull final Gson gson) {
    this.gson = checkNotNull(gson, "\'gson\' must not be null.");
  }
  
  @Override
  @Nonnull
  public Single<String> serialise(@Nonnull final RecoveryScheme scheme) {
    checkNotNull(scheme, "\'scheme\' must not be null.");
    
    return Single.create(emitter -> emitter.onSuccess(gson.toJson(scheme)));
  }
  
  @Override
  @Nonnull
  public Single<RecoveryScheme> deserialise(@Nonnull final String serialisedScheme) {
    checkNotNull(serialisedScheme, "\'serialisedScheme\' must not be null.");
    
    return Single.create(emitter -> emitter.onSuccess(doDeserialisation(serialisedScheme)));
  }
  
  @Override
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedScheme) {
    checkNotNull(serialisedScheme, "\'serialisedScheme\' must not be null.");
    
    return Single.create(emitter -> {
      try {
        if (doDeserialisation(serialisedScheme) == null) {
          emitter.onSuccess(false);
          
        } else {
          emitter.onSuccess(true);
        }
        
      } catch (final Exception e) {
        emitter.onSuccess(false);
      }
    });
  }
  
  private RecoveryScheme doDeserialisation(final String serialisedScheme) {
    return gson.fromJson(serialisedScheme, RecoveryScheme.class);
  }
}