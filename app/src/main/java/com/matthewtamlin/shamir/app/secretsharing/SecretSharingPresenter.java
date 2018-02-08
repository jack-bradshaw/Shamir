package com.matthewtamlin.shamir.app.secretsharing;

import com.matthewtamlin.shamir.app.secretsharing.creation.CreationPresenter;
import com.matthewtamlin.shamir.app.secretsharing.recovery.RecoveryPresenter;
import io.reactivex.Completable;

import javax.annotation.Nonnull;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

public class SecretSharingPresenter {
  private final CreationPresenter creationPresenter;
  
  private final RecoveryPresenter recoveryPresenter;
  
  public SecretSharingPresenter(
      @Nonnull final CreationPresenter creationPresenter,
      @Nonnull final RecoveryPresenter recoveryPresenter) {
    this.creationPresenter = checkNotNull(creationPresenter, "\'creationPresenter\' must not be null.");
    this.recoveryPresenter = checkNotNull(recoveryPresenter, "\'recoveryPresenter\' must not be null.");
  }
  
  @Nonnull
  public Completable startPresenting() {
    return creationPresenter.startPresenting().andThen(recoveryPresenter.startPresenting());
  }
  
  @Nonnull
  public Completable stopPresenting() {
    return creationPresenter.stopPresenting().andThen(recoveryPresenter.stopPresenting());
  }
}