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

import com.google.auto.value.AutoValue;
import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.rxutilities.None;
import com.matthewtamlin.shamir.app.secretsharing.encoding.SecretEncoder;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static com.matthewtamlin.shamir.app.rxutilities.Operators.filterOptionalAndUnwrap;
import static com.matthewtamlin.shamir.app.secretsharing.recovery.DismissibleError.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@SuppressWarnings("OptionalIsPresent")
public class RecoveryPresenter {
  private final AtomicBoolean currentlyPresenting = new AtomicBoolean(false);
  
  private final Object startStopPresentingLock = new Object();
  
  private final Set<Disposable> disposables = new HashSet<>();
  
  private final RecoveryView view;
  
  private final Scheduler presentationScheduler;
  
  private final Scheduler viewScheduler;
  
  private final RxShamir rxShamir;
  
  private final SecretEncoder secretEncoder;
  
  private final PersistenceOperations persistenceOperations;
  
  private final RxFiles rxFiles;
  
  public RecoveryPresenter(
      @Nonnull final RecoveryView view,
      @Nonnull final Scheduler presentationScheduler,
      @Nonnull final Scheduler viewScheduler,
      @Nonnull final RxShamir rxShamir,
      @Nonnull final SecretEncoder secretEncoder,
      @Nonnull final PersistenceOperations persistenceOperations,
      @Nonnull final RxFiles rxFiles) {
    
    this.view = checkNotNull(view, "\'view\' must not be null.");
    this.presentationScheduler = checkNotNull(presentationScheduler, "\'presentationScheduler\' must not be null.");
    this.viewScheduler = checkNotNull(viewScheduler, "\'viewScheduler\' must not be null.");
    this.rxShamir = checkNotNull(rxShamir, "\'rxShamir\' must not be null.");
    this.secretEncoder = checkNotNull(secretEncoder, "\'secretEncoder\' must not be null.");
    this.persistenceOperations = checkNotNull(persistenceOperations, "\'persistenceOperations\' must not be null.");
    this.rxFiles = checkNotNull(rxFiles, "\'rxFiles\' must not be null.");
  }
  
  @Nonnull
  public Completable startPresenting() {
    return Completable.create(emitter -> {
      synchronized (startStopPresentingLock) {
        if (currentlyPresenting.compareAndSet(false, true)) {
          subscribeToEvents();
        }
        
        emitter.onComplete();
      }
    });
  }
  
  @Nonnull
  public Completable stopPresenting() {
    return Completable.create(emitter -> {
      synchronized (startStopPresentingLock) {
        if (currentlyPresenting.compareAndSet(true, false)) {
          for (final Disposable disposable : disposables) {
            disposable.dispose();
          }
          
          disposables.clear();
        }
        
        emitter.onComplete();
      }
    });
  }
  
  private void subscribeToEvents() {
    final PublishSubject<Boolean> shareFilePathsAreSet = PublishSubject.create();
    final PublishSubject<Boolean> recoverySchemeFilePathIsSet = PublishSubject.create();
    final PublishSubject<Boolean> outputDirectoryPathIsSet = PublishSubject.create();
    final PublishSubject<None> anyInputIsNotSet = PublishSubject.create();
    final ReplaySubject<Boolean> allInputsAreSet = ReplaySubject.createWithSize(1);
    final ReplaySubject<InputModel> inputModel = ReplaySubject.createWithSize(1);
    final PublishSubject<InputModel> validRequest = PublishSubject.create();
    final PublishSubject<Optional<RecoveryModel>> recoveryModel = PublishSubject.create();
    final PublishSubject<Optional<Set<Share>>> shares = PublishSubject.create();
    final PublishSubject<Optional<RecoveryScheme>> recoveryScheme = PublishSubject.create();
    final PublishSubject<Optional<File>> recoveredSecretFile = PublishSubject.create();
    final PublishSubject<Optional<byte[]>> recoveredSecret = PublishSubject.create();
    final PublishSubject<None> recoveryOperationComplete = PublishSubject.create();
    
    disposables.add(shareFilePathsAreSet
        .observeOn(viewScheduler)
        .flatMapCompletable(isSet -> isSet ?
            view.enableClearSelectedShareFilesButton() :
            view.disableClearSelectedShareFilesButton())
        .subscribe());
    
    disposables.add(recoverySchemeFilePathIsSet
        .observeOn(viewScheduler)
        .flatMapCompletable(isSet ->
            isSet ?
                view.enableClearSelectedRecoverySchemeFileButton() :
                view.disableClearRecoverySchemeFileButton())
        .subscribe());
    
    disposables.add(outputDirectoryPathIsSet
        .observeOn(viewScheduler)
        .flatMapCompletable(isSet ->
            isSet ?
                view.enableClearSelectedOutputDirectoryButton() :
                view.disableClearSelectedOutputDirectoryButton())
        .subscribe());
    
    disposables.add(anyInputIsNotSet
        .observeOn(viewScheduler)
        .flatMapCompletable(none -> view.disableRecoverSecretRequests())
        .subscribe());
    
    disposables.add(allInputsAreSet
        .observeOn(presentationScheduler)
        .filter(TRUE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(allPass -> view.enableRecoverSecretRequests())
        .subscribe());
    
    disposables.add(view.observeRecoverSharesRequests()
        .observeOn(viewScheduler)
        .flatMapCompletable(model -> view.showRecoveryInProgress())
        .subscribe());
    
    disposables.add(recoveryOperationComplete
        .observeOn(viewScheduler)
        .flatMapCompletable(none -> view.showRecoveryNotInProgress())
        .subscribe());
    
    Observable
        .merge(
            shareFilePathsAreSet.filter(FALSE::equals),
            recoverySchemeFilePathIsSet.filter(FALSE::equals),
            outputDirectoryPathIsSet.filter(FALSE::equals))
        .map(val -> None.getInstance())
        .subscribe(anyInputIsNotSet);
    
    Observable
        .combineLatest(
            shareFilePathsAreSet,
            recoverySchemeFilePathIsSet,
            outputDirectoryPathIsSet,
            (shareSet, schemeSet, outputSet) -> shareSet && schemeSet && outputSet)
        .subscribe(allInputsAreSet);
    
    Observable
        .combineLatest(
            view.observeShareFilePaths()
                .observeOn(presentationScheduler)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMapSingle(filePathSet -> Observable
                    .fromIterable(filePathSet)
                    .map(File::new)
                    .collectInto(new HashSet<>(), Set::add)),
            view.observeRecoverySchemeFilePath()
                .observeOn(presentationScheduler)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(File::new),
            view.observeOutputDirectoryPath()
                .observeOn(presentationScheduler)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(File::new),
            InputModel::create)
        .subscribe(inputModel);
    
    Observable
        .zip(
            shares,
            recoveryScheme,
            recoveredSecretFile,
            (optionalShares, optionalRecoveryScheme, optionalOutputFile) -> {
              if (!optionalShares.isPresent()) {
                return Optional.<RecoveryModel>empty();
              }
              
              if (!optionalRecoveryScheme.isPresent()) {
                return Optional.<RecoveryModel>empty();
              }
              
              if (!optionalOutputFile.isPresent()) {
                return Optional.<RecoveryModel>empty();
              }
              
              return Optional.of(RecoveryModel.create(
                  optionalShares.get(),
                  optionalRecoveryScheme.get(),
                  optionalOutputFile.get()));
            })
        .subscribe(recoveryModel);
    
    validRequest
        .observeOn(presentationScheduler)
        .flatMapSingle(model -> loadSharesFromFiles(model.getShareFiles())
            .map(Optional::of)
            .observeOn(viewScheduler)
            .onErrorResumeNext(error -> view
                .showDismissibleError(FILESYSTEM_ERROR)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(Optional.empty()))))
        .observeOn(presentationScheduler)
        .subscribe(shares);
    
    validRequest
        .observeOn(presentationScheduler)
        .flatMapSingle(model -> loadRecoveryScheme(model.getRecoverySchemeFile())
            .map(Optional::of)
            .observeOn(viewScheduler)
            .onErrorResumeNext(error -> view
                .showDismissibleError(FILESYSTEM_ERROR)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(Optional.empty()))))
        .observeOn(presentationScheduler)
        .subscribe(recoveryScheme);
    
    validRequest
        .observeOn(presentationScheduler)
        .flatMapSingle(model -> createRecoveredSecretFile(model.getOutputDirectory())
            .map(Optional::of)
            .observeOn(viewScheduler)
            .onErrorResumeNext(error -> view
                .showDismissibleError(CANNOT_CREATE_RECOVERED_SECRET_FILE)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(Optional.empty()))))
        .observeOn(presentationScheduler)
        .subscribe(recoveredSecretFile);
    
    recoveryModel
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .flatMapSingle(model -> recoverSecret(model)
            .map(Optional::of)
            .observeOn(viewScheduler)
            .onErrorResumeNext(error -> view
                .showDismissibleError(RECOVERY_FAILED)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(rxFiles.delete(model.getRecoveredSecretFile()))
                .andThen(Single.just(Optional.empty()))))
        .observeOn(presentationScheduler)
        .subscribe(recoveredSecret);
    
    Observable
        .zip(
            recoveryModel,
            recoveredSecret,
            (model, secret) -> {
              if (!model.isPresent() || !secret.isPresent()) {
                return Completable.complete();
              }
              
              return rxFiles
                  .writeBytesToFile(secret.get(), model.get().getRecoveredSecretFile())
                  .observeOn(viewScheduler)
                  .onErrorResumeNext(error -> view
                      .showDismissibleError(CANNOT_WRITE_TO_RECOVERED_SECRET_FILE)
                      .andThen(view.showRecoveryNotInProgress())
                      .andThen(rxFiles.delete(model.get().getRecoveredSecretFile()))
                      .andThen(Completable.complete()));
            })
        .observeOn(presentationScheduler)
        .flatMap(wrappedCompletable -> wrappedCompletable.andThen(Observable.just(None.getInstance())))
        .subscribe(recoveryOperationComplete);
    
    view.observeShareFilePaths()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .subscribe(shareFilePathsAreSet);
    
    view.observeRecoverySchemeFilePath()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .subscribe(recoverySchemeFilePathIsSet);
    
    view.observeOutputDirectoryPath()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .subscribe(outputDirectoryPathIsSet);
    
    view.observeRecoverSharesRequests()
        .observeOn(presentationScheduler)
        .map(request -> {
          if (!allInputsAreSet.hasValue() || !inputModel.hasValue()) {
            return Optional.<InputModel>empty();
          }
          
          if (!allInputsAreSet.getValue()) {
            return Optional.<InputModel>empty();
          }
          
          return Optional.of(inputModel.getValue());
        })
        .compose(filterOptionalAndUnwrap())
        .flatMapSingle(model -> shareFilesAreValid(model.getShareFiles())
            .map(valid -> valid ? Optional.of(model) : Optional.<InputModel>empty()))
        .compose(filterOptionalAndUnwrap())
        .flatMapSingle(model -> recoverySchemeFilesIsValid(model.getRecoverySchemeFile())
            .map(valid -> valid ? Optional.of(model) : Optional.<InputModel>empty()))
        .compose(filterOptionalAndUnwrap())
        .flatMapSingle(model -> outputDirectoryIsValid(model.getOutputDirectory())
            .map(valid -> valid ? Optional.of(model) : Optional.<InputModel>empty()))
        .compose(filterOptionalAndUnwrap())
        .subscribe(validRequest);
  }
  
  private Single<Boolean> shareFilesAreValid(final Set<File> files) {
    final Single<Boolean> shareFilesExist = Observable
        .fromIterable(files)
        .flatMapSingle(rxFiles::exists)
        .reduce(true, (cumulative, thisExists) -> cumulative && thisExists)
        .observeOn(viewScheduler)
        .flatMap(allExist -> allExist ?
            Single.just(true) :
            view.showDismissibleError(SHARE_FILE_DOES_NOT_EXIST)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> shareFilesContainValidData = Observable
        .fromIterable(files)
        .flatMapSingle(persistenceOperations::fileContainsShare)
        .reduce(true, (cumulative, thisContainsShare) -> cumulative && thisContainsShare)
        .observeOn(viewScheduler)
        .flatMap(allValidData -> allValidData ?
            Single.just(true) :
            view.showDismissibleError(SHARE_FILE_IS_MALFORMED)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    return shareFilesExist
        .flatMap(filesExist -> filesExist ? shareFilesContainValidData : Single.just(false))
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showRecoveryNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
  }
  
  private Single<Boolean> recoverySchemeFilesIsValid(final File file) {
    final Single<Boolean> recoverSchemeFileExists = rxFiles
        .exists(file)
        .observeOn(viewScheduler)
        .flatMap(exists -> exists ?
            Single.just(true) :
            view.showDismissibleError(RECOVERY_SCHEME_FILE_DOES_NOT_EXIST)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> recoverySchemeContainsValidData = persistenceOperations
        .fileContainsRecoveryScheme(file)
        .observeOn(viewScheduler)
        .flatMap(validData -> validData ?
            Single.just(true) :
            view.showDismissibleError(RECOVERY_SCHEME_IS_MALFORMED)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    return recoverSchemeFileExists
        .flatMap(exists -> exists ? recoverySchemeContainsValidData : Single.just(false))
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showRecoveryNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
  }
  
  private Single<Boolean> outputDirectoryIsValid(final File outputDirectory) {
    final Single<Boolean> outputDirectoryExists = rxFiles
        .exists(outputDirectory)
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showRecoveryNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler)
        .flatMap(exists -> exists ?
            Single.just(true) :
            rxFiles.createDirectory(outputDirectory).andThen(Single.just(true)))
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(OUTPUT_DIRECTORY_CANNOT_BE_CREATED)
            .andThen(view.showRecoveryNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> outputDirectoryIsClean = persistenceOperations
        .directoryContainsRecoveredSecretFile(outputDirectory)
        .map(FALSE::equals)
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showRecoveryNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    return outputDirectoryExists
        .flatMap(exists -> exists ? outputDirectoryIsClean : Single.just(false))
        .observeOn(viewScheduler)
        .flatMap(exists -> exists ?
            Single.just(true) :
            view.showDismissibleError(OUTPUT_DIRECTORY_IS_NOT_CLEAN)
                .andThen(view.showRecoveryNotInProgress())
                .andThen(Single.just(false)))
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showRecoveryNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
  }
  
  private Single<Set<Share>> loadSharesFromFiles(final Set<File> files) {
    return Observable
        .fromIterable(files)
        .flatMapSingle(persistenceOperations::loadShareFromFile)
        .collectInto(new HashSet<>(), Set::add);
  }
  
  private Single<RecoveryScheme> loadRecoveryScheme(final File file) {
    return persistenceOperations.loadRecoverySchemeFromFile(file);
  }
  
  private Single<byte[]> recoverSecret(final RecoveryModel recoveryModel) {
    return rxShamir
        .recoverSecret(recoveryModel.getShares(), recoveryModel.getRecoveryScheme())
        .flatMap(secretEncoder::decodeSecret);
  }
  
  private Single<File> createRecoveredSecretFile(final File outputDirectory) {
    final Single<File> defineFile = persistenceOperations.defineNewRecoveredSecretFile(outputDirectory);
    
    return defineFile.flatMap(file -> rxFiles
        .createNewFile(file)
        .andThen(Single.just(file)));
  }
  
  @AutoValue
  protected static abstract class InputModel {
    public abstract Set<File> getShareFiles();
    
    public abstract File getRecoverySchemeFile();
    
    public abstract File getOutputDirectory();
    
    public static InputModel create(
        final Set<File> shareFiles,
        final File recoverySchemeFile,
        final File outputDirectory) {
      
      return new AutoValue_RecoveryPresenter_InputModel(shareFiles, recoverySchemeFile, outputDirectory);
    }
  }
  
  @AutoValue
  protected static abstract class RecoveryModel {
    public abstract Set<Share> getShares();
    
    public abstract RecoveryScheme getRecoveryScheme();
    
    public abstract File getRecoveredSecretFile();
    
    public static RecoveryModel create(
        final Set<Share> shares,
        final RecoveryScheme recoveryScheme,
        final File recoveredSecretFile) {
      
      return new AutoValue_RecoveryPresenter_RecoveryModel(shares, recoveryScheme, recoveredSecretFile);
    }
  }
}