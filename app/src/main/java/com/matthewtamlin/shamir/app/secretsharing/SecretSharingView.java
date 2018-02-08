package com.matthewtamlin.shamir.app.secretsharing;

import com.matthewtamlin.shamir.app.secretsharing.creation.CreationView;
import com.matthewtamlin.shamir.app.secretsharing.recovery.RecoveryView;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;

public class SecretSharingView extends VBox {
  public SecretSharingView(@Nonnull final CreationView creationView, @Nonnull final RecoveryView recoveryView) {
    getChildren().add(creationView);
    getChildren().add(recoveryView);
  }
}