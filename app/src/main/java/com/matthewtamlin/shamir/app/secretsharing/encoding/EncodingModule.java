package com.matthewtamlin.shamir.app.secretsharing.encoding;

import com.matthewtamlin.shamir.app.secretsharing.SecretSharingScope;
import dagger.Module;
import dagger.Provides;

@Module
public class EncodingModule {
  @Provides
  @SecretSharingScope
  public SecretEncoder provideSecretEncoder() {
    return new SecretEncoder();
  }
}