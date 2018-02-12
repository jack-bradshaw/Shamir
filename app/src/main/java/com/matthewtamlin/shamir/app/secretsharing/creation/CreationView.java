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

package com.matthewtamlin.shamir.app.secretsharing.creation;

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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.Optional;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static com.matthewtamlin.shamir.app.secretsharing.creation.DismissibleError.*;
import static com.matthewtamlin.shamir.app.secretsharing.creation.PersistentError.*;
import static java.lang.String.format;

public class CreationView extends StackPane {
  private static final Map<DismissibleError, String> DISMISSIBLE_ERROR_RESOURCE_KEYS = ImmutableMap
      .<DismissibleError, String>builder()
      .put(SECRET_FILE_DOES_NOT_EXIST, "creationView_dismissibleError_secretFileDoesNotExist")
      .put(OUTPUT_DIRECTORY_CANNOT_BE_CREATED, "creationView_dismissibleError_outputDirectoryCannotBeCreated")
      .put(OUTPUT_DIRECTORY_IS_NOT_CLEAN, "creationView_dismissibleError_outputDirectoryIsNotClean")
      .put(CANNOT_CREATE_SHARE_FILE, "creationView_dismissibleError_cannotCreateShareFile")
      .put(CANNOT_WRITE_TO_SHARE_FILE, "creationView_dismissibleError_cannotWriteToShareFile")
      .put(CANNOT_CREATE_RECOVERY_SCHEME_FILE, "creationView_dismissibleError_cannotCreateRecoverySchemeFile")
      .put(CANNOT_WRITE_TO_RECOVERY_SCHEME_FILE, "creationView_dismissibleError_cannotWriteToRecoverySchemeFile")
      .put(SECRET_FILE_IS_TOO_LARGE, "creationView_dismissibleError_secretFileIsTooLarge")
      .put(FILESYSTEM_ERROR, "creationView_dismissibleError_filesystemError")
      .build();
  
  private final ReplaySubject<Optional<Integer>> requiredShareCount = ReplaySubject.createWithSize(1);
  
  private final ReplaySubject<Optional<Integer>> totalShareCount = ReplaySubject.createWithSize(1);
  
  private final ReplaySubject<Optional<String>> secretFilePath = ReplaySubject.createWithSize(1);
  
  private final ReplaySubject<Optional<String>> outputDirectoryPath = ReplaySubject.createWithSize(1);
  
  private final PublishSubject<None> createSharesRequests = PublishSubject.create();
  
  private final Node controlContainer;
  
  private final Label requiredShareCountErrorLabel;
  
  private final Label totalShareCountErrorLabel;
  
  private final Button clearSecretFileButton;
  
  private final Button clearOutputDirectoryButton;
  
  private final Node createSharesButton;
  
  private final Resources resources;
  
  public CreationView(@Nonnull final Resources resources, @Nonnull final RxFilePicker rxFilePicker) {
    this.resources = checkNotNull(resources, "\'resources\' must not be null.");
    checkNotNull(rxFilePicker, "\'rxFilePicker\' must not be null.");
    
    getChildren().add(resources.blockingGetLayout("creation_view.fxml"));
    
    controlContainer = lookup("#control_container");
    
    final Label requiredShareCountLabel = (Label) lookup("#required_share_count_label");
    final TextField requiredShareCountField = (TextField) lookup("#required_share_count_field");
    final Label totalShareCountLabel = (Label) lookup("#total_share_count_label");
    final TextField totalShareCountField = (TextField) lookup("#total_share_count_field");
    final Label secretFileLabel = (Label) lookup("#secret_file_label");
    final Button selectSecretFileButton = (Button) lookup("#select_secret_file_button");
    final Label outputDirectoryLabel = (Label) lookup("#output_directory_label");
    final Button selectOutputDirectoryButton = (Button) lookup("#select_output_directory_button");
    final Label createSharesButtonLabel = (Label) lookup("#create_shares_button_label");
    
    requiredShareCountErrorLabel = (Label) lookup("#required_share_count_error");
    totalShareCountErrorLabel = (Label) lookup("#total_share_count_error");
    clearSecretFileButton = (Button) lookup("#clear_secret_file_button");
    clearOutputDirectoryButton = (Button) lookup("#clear_output_directory_button");
    createSharesButton = lookup("#create_shares_button");
    
    requiredShareCountLabel.setText(resources.blockingGetString("creationView_requiredShareCountLabel"));
    totalShareCountLabel.setText(resources.blockingGetString("creationView_totalShareCountLabel"));
    secretFileLabel.setText(resources.blockingGetString("creationView_secretFileLabel"));
    selectSecretFileButton.setText(resources.blockingGetString("creationView_selectSecretFileButtonLabel"));
    outputDirectoryLabel.setText(resources.blockingGetString("creationView_outputDirectoryLabel"));
    selectOutputDirectoryButton.setText(resources.blockingGetString("creationView_selectOutputDirectoryButtonLabel"));
    createSharesButtonLabel.setText(resources.blockingGetString("creationView_createSharesButtonLabel"));
    
    clearSecretFileButton.setText(resources.blockingGetString("creationView_clearSecretFileButtonLabel"));
    clearOutputDirectoryButton.setText(resources.blockingGetString("creationView_clearOutputDirectoryButtonLabel"));
    
    setNumericOnly(requiredShareCountField);
    setNumericOnly(totalShareCountField);
    
    JavaFxObservable
        .valuesOf(requiredShareCountField.textProperty())
        .filter(text -> text.matches("\\d*"))
        .map(text -> text.isEmpty() ? Optional.<Integer>empty() : Optional.of(Integer.valueOf(text)))
        .subscribe(requiredShareCount::onNext);
    
    JavaFxObservable
        .valuesOf(totalShareCountField.textProperty())
        .filter(text -> text.matches("\\d*"))
        .map(text -> text.isEmpty() ? Optional.<Integer>empty() : Optional.of(Integer.valueOf(text)))
        .subscribe(totalShareCount::onNext);
    
    JavaFxObservable
        .actionEventsOf(selectSecretFileButton)
        .flatMapSingle(event -> rxFilePicker.pickFile(null))
        .map(optionalFile -> optionalFile.map(File::getAbsolutePath))
        .startWith(Optional.empty())
        .subscribe(secretFilePath);
    
    JavaFxObservable
        .actionEventsOf(selectOutputDirectoryButton)
        .flatMapSingle(event -> rxFilePicker.pickDirectory(null))
        .map(optionalFile -> optionalFile.map(File::getAbsolutePath))
        .startWith(Optional.empty())
        .subscribe(outputDirectoryPath);
    
    JavaFxObservable
        .actionEventsOf(clearSecretFileButton)
        .map(event -> Optional.<String>empty())
        .subscribe(secretFilePath);
    
    JavaFxObservable
        .actionEventsOf(clearOutputDirectoryButton)
        .map(event -> Optional.<String>empty())
        .subscribe(outputDirectoryPath);
    
    JavaFxObservable
        .eventsOf(createSharesButton, MouseEvent.MOUSE_CLICKED)
        .map(event -> None.getInstance())
        .subscribe(createSharesRequests);
  }
  
  @Nonnull
  public Observable<Optional<Integer>> observeRequiredShareCount() {
    return requiredShareCount;
  }
  
  @Nonnull
  public Observable<Optional<Integer>> observeTotalShareCount() {
    return totalShareCount;
  }
  
  @Nonnull
  public Observable<Optional<String>> observeSecretFilePath() {
    return secretFilePath;
  }
  
  @Nonnull
  public Observable<Optional<String>> observeOutputDirectoryPath() {
    return outputDirectoryPath;
  }
  
  @Nonnull
  public Observable<None> observeCreateSharesRequests() {
    return createSharesRequests;
  }
  
  @Nonnull
  public Completable enableCreateSharesRequests() {
    return Completable.fromRunnable(() -> {
      createSharesButton.setOpacity(1);
      createSharesButton.setDisable(false);
    });
  }
  
  @Nonnull
  public Completable disableCreateSharesRequests() {
    return Completable.fromRunnable(() -> {
      createSharesButton.setOpacity(0.5);
      createSharesButton.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable showPersistentError(@Nonnull final PersistentError error) {
    if (error == FEWER_THAN_TWO_REQUIRED_SHARES) {
      return resources
          .getString("creationViewPersistentError_fewerThanTwoRequiredShares")
          .flatMapCompletable(text -> Completable.fromRunnable(() -> requiredShareCountErrorLabel.setText(text)));
      
    } else if (error == FEWER_THAN_TWO_TOTAL_SHARES) {
      return resources
          .getString("creationViewPersistentError_fewerThanTwoTotalShares")
          .flatMapCompletable(text -> Completable.fromRunnable(() -> totalShareCountErrorLabel.setText(text)));
      
    } else if (error == FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES) {
      return resources
          .getString("creationViewPersistentError_fewerTotalSharesThanRequiredShares")
          .flatMapCompletable(text -> Completable.fromRunnable(() -> requiredShareCountErrorLabel.setText(text)));
      
    } else {
      throw new RuntimeException(format("Unexpected persistent error: \'%1$s\'", error));
    }
  }
  
  @Nonnull
  public Completable hidePersistentError(@Nonnull final PersistentError error) {
    if (error == FEWER_THAN_TWO_REQUIRED_SHARES || error == FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES) {
      return Completable.fromRunnable(() -> requiredShareCountErrorLabel.setText(null));
      
    } else if (error == FEWER_THAN_TWO_TOTAL_SHARES) {
      return Completable.fromRunnable(() -> totalShareCountErrorLabel.setText(null));
      
    } else {
      throw new RuntimeException(format("Unexpected persistent error: \'%1$s\'", error));
    }
  }
  
  @Nonnull
  public Completable showDismissibleError(@Nonnull final DismissibleError error) {
    return Single
        .zip(
            resources.getString("creationView_dismissibleError_title"),
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
  public Completable enableClearSelectedSecretFileButton() {
    return Completable.fromRunnable(() -> {
      clearSecretFileButton.setOpacity(1);
      clearSecretFileButton.setDisable(false);
    });
  }
  
  @Nonnull
  public Completable disableClearSelectedSecretFileButton() {
    return Completable.fromRunnable(() -> {
      clearSecretFileButton.setOpacity(0.5);
      clearSecretFileButton.setDisable(true);
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
  public Completable showShareCreationInProgress() {
    return Completable.fromRunnable(() -> {
      controlContainer.setOpacity(0.5);
      controlContainer.setDisable(true);
    });
  }
  
  @Nonnull
  public Completable showShareCreationNotInProgress() {
    return Completable.fromRunnable(() -> {
      controlContainer.setOpacity(1);
      controlContainer.setDisable(false);
    });
  }
  
  private void setNumericOnly(final TextField textField) {
    JavaFxObservable
        .valuesOf(textField.textProperty())
        .filter(text -> !text.matches("\\d*"))
        .map(text -> text.replaceAll("[^\\d]", ""))
        .subscribe(textField::setText);
  }
}