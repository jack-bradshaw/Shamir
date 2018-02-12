/*
 * Copyright 2018 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.shamir.app.secretsharing.recovery;

import com.google.common.collect.ImmutableMap;
import com.matthewtamlin.shamir.app.files.RxFilePicker;
import com.matthewtamlin.shamir.app.resources.Resources;
import com.matthewtamlin.shamir.app.rxutilities.None;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static com.matthewtamlin.shamir.app.secretsharing.recovery.DismissibleError.*;

public class RecoveryView extends StackPane {
  private static final Map<DismissibleError, String> DISMISSIBLE_ERROR_RESOURCE_KEYS = ImmutableMap
      .<DismissibleError, String>builder()
      .put(SHARE_FILE_DOES_NOT_EXIST, "recoveryView_dismissibleError_shareFileDoesNotExist")
      .put(SHARE_FILE_IS_MALFORMED, "recoveryView_dismissibleError_shareFileIsMalformed")
      .put(RECOVERY_SCHEME_FILE_DOES_NOT_EXIST, "recoveryView_dismissibleError_recoverySchemeFileDoesNotExist")
      .put(RECOVERY_SCHEME_IS_MALFORMED, "recoveryView_dismissibleError_recoverySchemeIsMalformed")
      .put(OUTPUT_DIRECTORY_CANNOT_BE_CREATED, "recoveryView_dismissibleError_outputDirectoryCannotBeCreated")
      .put(OUTPUT_DIRECTORY_IS_NOT_CLEAN, "recoveryView_dismissibleError_outputDirectoryIsNotClean")
      .put(CANNOT_CREATE_RECOVERED_SECRET_FILE, "recoveryView_dismissibleError_cannotCreateRecoveredSecretFile")
      .put(CANNOT_WRITE_TO_RECOVERED_SECRET_FILE, "recoveryView_dismissibleError_cannotWriteToRecoveredSecretFile")
      .put(RECOVERY_FAILED, "recoveryView_dismissibleError_recoveryFailed")
      .put(FILESYSTEM_ERROR, "recoveryView_dismissibleError_filesystemError")
      .build();
  
  private final ReplaySubject<Optional<? extends Set<String>>> shareFilesPath = ReplaySubject.createWithSize(1);
  
  private final ReplaySubject<Optional<String>> recoverySchemeFilePath = ReplaySubject.createWithSize(1);
  
  private final ReplaySubject<Optional<String>> outputDirectoryPath = ReplaySubject.createWithSize(1);
  
  private final PublishSubject<None> recoverSecretRequest = PublishSubject.create();
  
  private final Node controlsContainer;
  
  private final Button clearShareFilesButton;
  
  private final Button clearRecoverySchemeFileButton;
  
  private final Button clearOutputDirectoryButton;
  
  private final Node recoverSecretButton;
  
  private final Resources resources;
  
  public RecoveryView(@Nonnull final Resources resources, @Nonnull final RxFilePicker rxFilePicker) {
    this.resources = checkNotNull(resources, "\'resources\' must not be null.");
    checkNotNull(rxFilePicker, "\'rxFilePicker\' must not be null.");
    
    getChildren().add(resources.blockingGetLayout("recovery_view.fxml"));
    
    controlsContainer = lookup("#controls_container");
    
    final Label shareFilesLabel = (Label) lookup("#share_files_label");
    final Button selectShareFilesButton = (Button) lookup("#select_share_files_button");
    final Label recoverySchemeFileLabel = (Label) lookup("#recovery_scheme_file_label");
    final Button selectRecoverySchemeFileButton = (Button) lookup("#select_recovery_scheme_file_button");
    final Label outputDirectoryLabel = (Label) lookup("#output_directory_label");
    final Button selectOutputDirectoryButton = (Button) lookup("#select_output_directory_button");
    final Label recoverSecretButtonLabel = (Label) lookup("#recover_secret_button_label");
    
    clearShareFilesButton = (Button) lookup("#clear_share_files_button");
    clearRecoverySchemeFileButton = (Button) lookup("#clear_recovery_scheme_file_button");
    clearOutputDirectoryButton = (Button) lookup("#clear_output_directory_button");
    recoverSecretButton = lookup("#recover_secret_button");
    
    shareFilesLabel.setText(resources.blockingGetString("recoveryView_shareFilesLabel"));
    selectShareFilesButton.setText(resources.blockingGetString("recoveryView_selectShareFilesButtonLabel"));
    recoverySchemeFileLabel.setText(resources.blockingGetString("recoveryView_recoverySchemeFileLabel"));
    selectRecoverySchemeFileButton.setText(
        resources.blockingGetString("recoveryView_selectRecoverySchemeFileButtonLabel"));
    outputDirectoryLabel.setText(resources.blockingGetString("recoveryView_outputDirectoryLabel"));
    selectOutputDirectoryButton.setText(resources.blockingGetString("recoveryView_selectOutputDirectoryButtonLabel"));
    recoverSecretButtonLabel.setText(resources.blockingGetString("recoveryView_recoverSecretButtonLabel"));
    
    clearShareFilesButton.setText(resources.blockingGetString("recoveryView_clearShareFilesButtonLabel"));
    clearRecoverySchemeFileButton.setText(
        resources.blockingGetString("recoveryView_clearRecoverySchemeFileButtonLabel"));
    clearOutputDirectoryButton.setText(resources.blockingGetString("recoveryView_clearOutputDirectoryButtonLabel"));
    
    JavaFxObservable
        .actionEventsOf(selectShareFilesButton)
        .flatMapSingle(event -> rxFilePicker.pickFiles(null))
        .flatMapSingle(optionalFiles -> optionalFiles.isPresent() ?
            Observable
                .fromIterable(optionalFiles.get())
                .map(File::getAbsolutePath)
                .collectInto(new HashSet<String>(), Set::add)
                .map(Optional::of) :
            Single.just(Optional.<Set<String>>empty()))
        .startWith(Optional.empty())
        .subscribe(shareFilesPath);
    
    JavaFxObservable
        .actionEventsOf(selectRecoverySchemeFileButton)
        .flatMapSingle(event -> rxFilePicker.pickFile(null))
        .map(optionalFile -> optionalFile.map(File::getAbsolutePath))
        .startWith(Optional.empty())
        .subscribe(recoverySchemeFilePath);
    
    JavaFxObservable
        .actionEventsOf(selectOutputDirectoryButton)
        .flatMapSingle(event -> rxFilePicker.pickDirectory(null))
        .map(optionalFile -> optionalFile.map(File::getAbsolutePath))
        .startWith(Optional.empty())
        .subscribe(outputDirectoryPath);
    
    JavaFxObservable
        .eventsOf(recoverSecretButton, MouseEvent.MOUSE_CLICKED)
        .map(event -> None.getInstance())
        .subscribe(recoverSecretRequest);
    
    JavaFxObservable
        .actionEventsOf(clearShareFilesButton)
        .map(event -> Optional.<Set<String>>empty())
        .subscribe(shareFilesPath);
    
    JavaFxObservable
        .actionEventsOf(clearRecoverySchemeFileButton)
        .map(event -> Optional.<String>empty())
        .subscribe(recoverySchemeFilePath);
    
    JavaFxObservable
        .actionEventsOf(clearOutputDirectoryButton)
        .map(event -> Optional.<String>empty())
        .subscribe(outputDirectoryPath);
  }
  
  @Nonnull
  public Observable<Optional<? extends Set<String>>> observeShareFilePaths() {
    return shareFilesPath;
  }
  
  @Nonnull
  public Observable<Optional<String>> observeRecoverySchemeFilePath() {
    return recoverySchemeFilePath;
  }
  
  @Nonnull
  public Observable<Optional<String>> observeOutputDirectoryPath() {
    return outputDirectoryPath;
  }
  
  @Nonnull
  public Observable<None> observeRecoverSharesRequests() {
    return recoverSecretRequest;
  }
  
  @Nonnull
  public Completable enableRecoverSecretRequests() {
    return Completable.fromRunnable(() -> {
      recoverSecretButton.setOpacity(1);
      recoverSecretButton.setDisable(false);
    });
  }
  
  @Nonnull
  public Completable disableRecoverSecretRequests() {
    return Completable.fromRunnable(() -> {
      recoverSecretButton.setOpacity(0.5);
      recoverSecretButton.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable showDismissibleError(@Nonnull DismissibleError error) {
    return Single
        .zip(
            resources.getString("recoveryView_dismissibleError_title"),
            resources.getString(DISMISSIBLE_ERROR_RESOURCE_KEYS.get(error)),
            (title, body) -> {
              final Alert alert = new Alert(AlertType.ERROR);
              
              alert.setTitle(title);
              alert.setHeaderText(null);
              alert.setContentText(body);
              
              return alert;
            })
        .flatMapMaybe(JavaFxObservable::fromDialog)
        .flatMapCompletable(response -> Completable.complete());
  }
  
  @Nonnull
  public Completable enableClearSelectedShareFilesButton() {
    return Completable.fromRunnable(() -> {
      clearShareFilesButton.setOpacity(1);
      clearShareFilesButton.setDisable(false);
    });
  }
  
  @Nonnull
  public Completable disableClearSelectedShareFilesButton() {
    return Completable.fromRunnable(() -> {
      clearShareFilesButton.setOpacity(0.5);
      clearShareFilesButton.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable enableClearSelectedRecoverySchemeFileButton() {
    return Completable.fromRunnable(() -> {
      clearRecoverySchemeFileButton.setOpacity(1);
      clearRecoverySchemeFileButton.setDisable(false);
    });
  }
  
  @Nonnull
  public Completable disableClearRecoverySchemeFileButton() {
    return Completable.fromRunnable(() -> {
      clearRecoverySchemeFileButton.setOpacity(0.5);
      clearRecoverySchemeFileButton.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable enableClearSelectedOutputDirectoryButton() {
    return Completable.fromRunnable(() -> {
      clearOutputDirectoryButton.setOpacity(1);
      clearOutputDirectoryButton.setDisable(false);
    });
  }
  
  @Nonnull
  public Completable disableClearSelectedOutputDirectoryButton() {
    return Completable.fromRunnable(() -> {
      clearOutputDirectoryButton.setOpacity(0.5);
      clearOutputDirectoryButton.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable showRecoveryInProgress() {
    return Completable.fromRunnable(() -> {
      controlsContainer.setOpacity(0.5);
      controlsContainer.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable showRecoveryNotInProgress() {
    return Completable.fromRunnable(() -> {
      controlsContainer.setOpacity(1);
      controlsContainer.setDisable(false);
    });
  }
}