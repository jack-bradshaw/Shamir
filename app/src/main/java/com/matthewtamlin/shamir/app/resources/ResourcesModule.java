package com.matthewtamlin.shamir.app.resources;

import com.matthewtamlin.shamir.app.AppScope;
import dagger.Module;
import dagger.Provides;

@Module
public class ResourcesModule {
  @Provides
  @AppScope
  public Resources provideResources() {
    return new Resources();
  }
}