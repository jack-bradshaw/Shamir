package com.matthewtamlin.shamir.app.secretsharing.recovery;

import com.matthewtamlin.shamir.app.files.RxFilePicker;
import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.resources.Resources;
import com.matthewtamlin.shamir.app.secretsharing.SecretSharingScope;
import com.matthewtamlin.shamir.app.secretsharing.encoding.SecretEncoder;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.RecoverySchemeSerialiser;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.ShareSerialiser;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Named;

@Module
public class RecoveryModule {
  @Provides
  @SecretSharingScope
  public RecoveryView provideRecoveryView(final Resources resources, final RxFilePicker rxFilePicker) {
    return new RecoveryView(resources, rxFilePicker);
  }
  
  @Provides
  @SecretSharingScope
  @Named("recoveryPresentationScheduler")
  public Scheduler providePresentationScheduler() {
    return Schedulers.newThread();
  }
  
  @Provides
  @SecretSharingScope
  @Named("recoveryViewScheduler")
  public Scheduler provideViewScheduler() {
    return JavaFxScheduler.platform();
  }
  
  @Provides
  @SecretSharingScope
  public PersistenceOperations providePersistenceOperations(
      final ShareSerialiser shareSerialiser,
      final RecoverySchemeSerialiser recoverySchemeSerialiser,
      final RxFiles rxFiles) {
    
    return new PersistenceOperations(shareSerialiser, recoverySchemeSerialiser, rxFiles);
  }
  
  @Provides
  @SecretSharingScope
  public RecoveryPresenter provideRecoveryPresenter(
      final RecoveryView recoveryView,
      @Named("recoveryPresentationScheduler") final Scheduler presentationScheduler,
      @Named("recoveryViewScheduler") final Scheduler viewScheduler,
      final RxShamir rxShamir,
      final SecretEncoder secretEncoder,
      final PersistenceOperations persistenceOperations,
      final RxFiles rxFiles) {
    
    return new RecoveryPresenter(
        recoveryView,
        presentationScheduler,
        viewScheduler,
        rxShamir,
        secretEncoder,
        persistenceOperations,
        rxFiles);
  }
}