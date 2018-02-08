package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.google.gson.Gson;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Single;

import javax.annotation.Nonnull;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;

public class JsonShareSerialiser implements ShareSerialiser {
  private Gson gson;
  
  public JsonShareSerialiser(@Nonnull final Gson gson) {
    this.gson = checkNotNull(gson, "\'gson\' must not be null.");
  }
  
  @Override
  @Nonnull
  public Single<String> serialise(@Nonnull final Share share) {
    checkNotNull(share, "\'share\' must not be null.");
    
    return Single.create(emitter -> emitter.onSuccess(gson.toJson(share)));
  }
  
  @Override
  @Nonnull
  public Single<Share> deserialise(@Nonnull final String serialisedShare) {
    checkNotNull(serialisedShare, "\'serialisedShare\' must not be null.");
    
    return Single.create(emitter -> emitter.onSuccess(doDeserialisation(serialisedShare)));
  }
  
  @Override
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedShare) {
    checkNotNull(serialisedShare, "\'serialisedShare\' must not be null.");
    
    return Single.create(emitter -> {
      try {
        if (doDeserialisation(serialisedShare) == null) {
          emitter.onSuccess(false);
          
        } else {
          emitter.onSuccess(true);
        }
        
      } catch (final DeserialisationException e) {
        emitter.onSuccess(false);
      }
    });
  }
  
  private Share doDeserialisation(final String serialisedShare) throws DeserialisationException {
    try {
      return gson.fromJson(serialisedShare, Share.class);
      
    } catch (final Exception e) {
      throw new DeserialisationException(format("Cannot deserialise %1$s", serialisedShare), e);
    }
  }
}