package com.matthewtamlin.shamir.app.secretsharing;

import com.matthewtamlin.shamir.app.secretsharing.creation.CreationPresenter;
import com.matthewtamlin.shamir.app.secretsharing.creation.CreationView;
import com.matthewtamlin.shamir.app.secretsharing.recovery.RecoveryPresenter;
import com.matthewtamlin.shamir.app.secretsharing.recovery.RecoveryView;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import dagger.Module;
import dagger.Provides;
import javafx.scene.Scene;

import java.security.SecureRandom;

@Module
public class SecretSharingModule {
  @Provides
  @SecretSharingScope
  public SecureRandom provideSecureRandom() {
    return new SecureRandom();
  }
  
  @Provides
  @SecretSharingScope
  public RxShamir provideRxShamir(final SecureRandom secureRandom) {
    return new RxShamir(secureRandom);
  }
  
  @Provides
  @SecretSharingScope
  public SecretSharingView provideSecretSharingView(final CreationView creationView, final RecoveryView recoveryView) {
    return new SecretSharingView(creationView, recoveryView);
  }
  
  @Provides
  @SecretSharingScope
  public Scene provideSecretSharingScene(final SecretSharingView secretSharingView) {
    return new Scene(secretSharingView);
  }
  
  @Provides
  @SecretSharingScope
  public SecretSharingPresenter provideSecretSharingPresenter(
      final CreationPresenter creationPresenter,
      final RecoveryPresenter recoveryPresenter) {
    
    return new SecretSharingPresenter(creationPresenter, recoveryPresenter);
  }
}