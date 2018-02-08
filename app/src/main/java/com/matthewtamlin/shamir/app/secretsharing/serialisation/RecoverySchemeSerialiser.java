package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import io.reactivex.Single;

import javax.annotation.Nonnull;

public interface RecoverySchemeSerialiser {
  @Nonnull
  public Single<String> serialise(@Nonnull final RecoveryScheme scheme);
  
  @Nonnull
  public Single<RecoveryScheme> deserialise(@Nonnull final String serialisedScheme);
  
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedScheme);
}