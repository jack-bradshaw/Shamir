package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Single;

import javax.annotation.Nonnull;

public interface ShareSerialiser {
  @Nonnull
  public Single<String> serialise(@Nonnull final Share share);
  
  @Nonnull
  public Single<Share> deserialise(@Nonnull final String serialisedShare);
  
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedShare);
}