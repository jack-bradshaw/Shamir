package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matthewtamlin.shamir.app.secretsharing.SecretSharingScope;
import com.matthewtamlin.shamir.commonslibrary.model.ModelTypeAdapterFactory;
import dagger.Module;
import dagger.Provides;

@Module
public class SerialisationModule {
  @Provides
  @SecretSharingScope
  public ModelTypeAdapterFactory provideModelTypeAdapterFactory() {
    return new ModelTypeAdapterFactory();
  }
  
  @Provides
  @SecretSharingScope
  public Gson provideGson(final ModelTypeAdapterFactory modelTypeAdapterFactory) {
    return new GsonBuilder()
        .registerTypeAdapterFactory(modelTypeAdapterFactory)
        .create();
  }
  
  @Provides
  @SecretSharingScope
  public ShareSerialiser provideShareSerialiser() {
    return new JsonShareSerialiser();
  }
  
  @Provides
  @SecretSharingScope
  public RecoverySchemeSerialiser provideRecoverySchemeSerialiser(final Gson gson) {
    return new JsonRecoverySchemeSerialiser(gson);
  }
}