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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.rxutilities.None;
import com.matthewtamlin.shamir.app.secretsharing.encoding.SecretEncoder;
import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.commonslibrary.util.Pair;
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
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static com.matthewtamlin.shamir.app.rxutilities.Operators.filterOptionalAndUnwrap;
import static com.matthewtamlin.shamir.app.secretsharing.creation.DismissibleError.*;
import static com.matthewtamlin.shamir.app.secretsharing.creation.PersistentError.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class CreationPresenter {
  private final AtomicBoolean currentlyPresenting = new AtomicBoolean(false);
  
  private final Object startStopPresentingLock = new Object();
  
  private final Set<Disposable> disposables = new HashSet<>();
  
  private final CreationView view;
  
  private final Scheduler presentationScheduler;
  
  private final Scheduler viewScheduler;
  
  private final RxShamir rxShamir;
  
  private final CryptoConstants cryptoConstants;
  
  private final SecretEncoder secretEncoder;
  
  private final PersistenceOperations persistenceOperations;
  
  private final RxFiles rxFiles;
  
  public CreationPresenter(
      @Nonnull final CreationView view,
      @Nonnull final Scheduler presentationScheduler,
      @Nonnull final Scheduler viewScheduler,
      @Nonnull final RxShamir rxShamir,
      @Nonnull final CryptoConstants cryptoConstants,
      @Nonnull final SecretEncoder secretEncoder,
      @Nonnull final PersistenceOperations persistenceOperations,
      @Nonnull final RxFiles rxFiles) {
    
    this.view = checkNotNull(view, "\'view\' must not be null.");
    this.presentationScheduler = checkNotNull(presentationScheduler, "\'presentationScheduler\' must not be null.");
    this.viewScheduler = checkNotNull(viewScheduler, "\'viewScheduler\' must not be null.");
    this.rxShamir = checkNotNull(rxShamir, "\'rxShamir\' must not be null.");
    this.cryptoConstants = checkNotNull(cryptoConstants, "\'cryptoConstants\' must not be null.");
    this.secretEncoder = checkNotNull(secretEncoder, "\'secretEncoder\' must not be null.");
    this.persistenceOperations = checkNotNull(persistenceOperations, "\'persistenceOperations\' must not be null.");
    this.rxFiles = checkNotNull(rxFiles, "\'rxFiles\' must not be null.");
  }
  
  @Nonnull
  public Completable startPresenting() {
    return Completable
        .create(emitter -> {
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
    return Completable
        .create(emitter -> {
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
    final PublishSubject<Optional<Boolean>> requiredShareCountExceeds1 = PublishSubject.create();
    final PublishSubject<Optional<Boolean>> totalCountExceeds1 = PublishSubject.create();
    final PublishSubject<Optional<Boolean>> requiredCountDoesNotExceedTotalCount = PublishSubject.create();
    final PublishSubject<Boolean> secretFileIsSet = PublishSubject.create();
    final PublishSubject<Boolean> outputDirectoryIsSet = PublishSubject.create();
    final ReplaySubject<Boolean> inputPreconditionsPass = ReplaySubject.createWithSize(1);
    final ReplaySubject<InputModel> inputModel = ReplaySubject.createWithSize(1);
    final PublishSubject<InputModel> validRequest = PublishSubject.create();
    final PublishSubject<Optional<BigInteger>> secret = PublishSubject.create();
    final PublishSubject<Optional<SharingResult>> sharingResult = PublishSubject.create();
    final PublishSubject<Optional<? extends Map<Share, File>>> shareFiles = PublishSubject.create();
    final PublishSubject<Optional<File>> recoverySchemeFile = PublishSubject.create();
    final PublishSubject<None> sharingOperationComplete = PublishSubject.create();
    
    disposables.add(view
        .observeRequiredShareCount()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> Completable.merge(ImmutableList.of(
            view.hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES),
            view.hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES))))
        .subscribe());
    
    disposables.add(view
        .observeRequiredShareCount()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(requiredShareCountExceeds1
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .observeOn(viewScheduler)
        .flatMapCompletable(exceeds1 -> exceeds1 ?
            view.hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES) :
            view.showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES))
        .subscribe());
    
    disposables.add(requiredShareCountExceeds1
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(view
        .observeTotalShareCount()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> Completable.merge(ImmutableList.of(
            view.hidePersistentError(FEWER_THAN_TWO_TOTAL_SHARES),
            view.hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES))))
        .subscribe());
    
    disposables.add(view
        .observeTotalShareCount()
        .observeOn(presentationScheduler)
        .map(Optional::isPresent)
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(totalCountExceeds1
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .observeOn(viewScheduler)
        .flatMapCompletable(exceeds1 -> exceeds1 ?
            view.hidePersistentError(FEWER_THAN_TWO_TOTAL_SHARES) :
            view.showPersistentError(FEWER_THAN_TWO_TOTAL_SHARES))
        .subscribe());
    
    disposables.add(totalCountExceeds1
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(requiredCountDoesNotExceedTotalCount
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .observeOn(viewScheduler)
        .flatMapCompletable(requiredShareCountExceedsTotalShareCount -> requiredShareCountExceedsTotalShareCount ?
            view.hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES) :
            view.showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES))
        .subscribe());
    
    disposables.add(requiredCountDoesNotExceedTotalCount
        .observeOn(presentationScheduler)
        .compose(filterOptionalAndUnwrap())
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(secretFileIsSet
        .observeOn(presentationScheduler)
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(outputDirectoryIsSet
        .observeOn(presentationScheduler)
        .filter(FALSE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(val -> view.disableCreateSharesRequests())
        .subscribe());
    
    disposables.add(inputPreconditionsPass
        .observeOn(presentationScheduler)
        .filter(TRUE::equals)
        .observeOn(viewScheduler)
        .flatMapCompletable(allPass -> view.enableCreateSharesRequests())
        .subscribe());
    
    disposables.add(secretFileIsSet
        .observeOn(viewScheduler)
        .flatMapCompletable(isSet -> isSet ?
            view.enableClearSelectedSecretFileButton() :
            view.disableClearSelectedSecretFileButton())
        .subscribe());
    
    disposables.add(outputDirectoryIsSet
        .observeOn(viewScheduler)
        .flatMapCompletable(isSet -> isSet ?
            view.enableClearSelectedOutputDirectoryButton() :
            view.disableClearSelectedOutputDirectoryButton())
        .subscribe());
    
    disposables.add(view.observeCreateSharesRequests()
        .observeOn(viewScheduler)
        .flatMapCompletable(model -> view.showShareCreationInProgress())
        .subscribe());
    
    disposables.add(sharingOperationComplete
        .observeOn(viewScheduler)
        .flatMapCompletable(none -> view.showShareCreationNotInProgress())
        .subscribe());
    
    Observable
        .combineLatest(
            view.observeRequiredShareCount(),
            view.observeTotalShareCount(),
            (optionalRequiredCount, optionalTotalCount) -> {
              if (!optionalRequiredCount.isPresent() || !optionalTotalCount.isPresent()) {
                return Optional.<Boolean>empty();
              }
              
              return Optional.of(optionalRequiredCount.get() <= optionalTotalCount.get());
            })
        .observeOn(presentationScheduler)
        .subscribe(requiredCountDoesNotExceedTotalCount);
    
    Observable
        .combineLatest(
            requiredShareCountExceeds1,
            totalCountExceeds1,
            requiredCountDoesNotExceedTotalCount,
            secretFileIsSet,
            outputDirectoryIsSet,
            this::inputPreconditionsPass)
        .subscribe(inputPreconditionsPass);
    
    Observable
        .combineLatest(
            view.observeRequiredShareCount().compose(filterOptionalAndUnwrap()),
            view.observeTotalShareCount().compose(filterOptionalAndUnwrap()),
            view.observeSecretFilePath().compose(filterOptionalAndUnwrap()),
            view.observeOutputDirectoryPath().compose(filterOptionalAndUnwrap()),
            this::createModel)
        .observeOn(presentationScheduler)
        .subscribe(inputModel);
    
    validRequest
        .flatMapSingle(model -> rxFiles
            .readBytesFromFile(model.getSecretFile())
            .flatMap(secretEncoder::encodeSecret)
            .map(Optional::of)
            .observeOn(viewScheduler)
            .onErrorResumeNext(
                error -> view.showDismissibleError(FILESYSTEM_ERROR).andThen(Single.just(Optional.empty()))))
        .observeOn(presentationScheduler)
        .subscribe(secret);
    
    Observable
        .zip(validRequest,
            secret,
            (model, secretVal) -> {
              if (!secretVal.isPresent()) {
                return Single.just(Optional.<SharingResult>empty());
              }
              
              return shareSecret(model, secretVal.get()).map(Optional::of);
            })
        .flatMapSingle(wrappedSingle -> wrappedSingle)
        .subscribe(sharingResult);
    
    Observable
        .zip(validRequest,
            sharingResult,
            (request, result) -> {
              if (!result.isPresent()) {
                return Single.just(Optional.<Map<Share, File>>empty());
              }
              
              return Observable
                  .fromIterable(result.get().getShares())
                  .flatMapSingle(share -> persistenceOperations
                      .defineNewShareFile(share, request.getOutputDirectory())
                      .flatMap(file -> rxFiles
                          .createNewFile(file)
                          .toSingle(() -> file))
                      .map(file -> Pair.create(share, file)))
                  .collectInto(
                      new HashMap<Share, File>(),
                      (map, pair) -> map.put(pair.getKey(), pair.getValue()))
                  .map(Optional::of)
                  .observeOn(viewScheduler)
                  .onErrorResumeNext(error -> view
                      .showDismissibleError(CANNOT_CREATE_SHARE_FILE)
                      .andThen(view.showShareCreationNotInProgress())
                      .andThen(Single.just(Optional.empty())))
                  .observeOn(presentationScheduler);
            })
        .flatMapSingle(wrappedSingle -> wrappedSingle)
        .subscribe(shareFiles);
    
    Observable
        .zip(validRequest,
            sharingResult,
            (request, result) -> {
              if (!result.isPresent()) {
                return Single.just(Optional.<File>empty());
              }
              
              return persistenceOperations
                  .defineNewRecoverySchemeFile(request.getOutputDirectory())
                  .flatMap(file -> rxFiles
                      .createNewFile(file)
                      .toSingle(() -> file))
                  .map(Optional::of)
                  .observeOn(viewScheduler)
                  .onErrorResumeNext(error -> view
                      .showDismissibleError(CANNOT_CREATE_RECOVERY_SCHEME_FILE)
                      .andThen(view.showShareCreationNotInProgress())
                      .andThen(Single.just(Optional.empty())))
                  .observeOn(presentationScheduler);
            })
        .flatMapSingle(wrappedSingle -> wrappedSingle)
        .subscribe(recoverySchemeFile);
    
    Observable
        .zip(
            validRequest,
            sharingResult,
            shareFiles,
            recoverySchemeFile,
            (request, result, shareFilesVal, recoverySchemeFileVal) -> {
              if (!result.isPresent()) {
                return Completable.complete();
              }
              
              if (!shareFilesVal.isPresent()) {
                return Completable.complete();
              }
              
              if (!recoverySchemeFileVal.isPresent()) {
                return Completable.complete();
              }
              
              final Completable saveShares = Observable
                  .fromIterable(result.get().getShares())
                  .flatMapCompletable(share -> persistenceOperations.saveShareToFile(
                      share,
                      shareFilesVal.get().get(share)))
                  .observeOn(viewScheduler)
                  .onErrorResumeNext(error -> view
                      .showDismissibleError(CANNOT_WRITE_TO_SHARE_FILE)
                      .andThen(view.showShareCreationNotInProgress())
                      .andThen(Completable.complete()))
                  .observeOn(presentationScheduler);
              
              final Completable saveRecoveryScheme = persistenceOperations
                  .saveRecoverySchemeToFile(
                      result.get().getRecoveryScheme(),
                      recoverySchemeFileVal.get())
                  .observeOn(viewScheduler)
                  .onErrorResumeNext(error -> view
                      .showDismissibleError(CANNOT_WRITE_TO_RECOVERY_SCHEME_FILE)
                      .andThen(view.showShareCreationNotInProgress())
                      .andThen(Completable.complete()))
                  .observeOn(presentationScheduler);
              
              return saveShares.andThen(saveRecoveryScheme);
            })
        .flatMap(wrappedCompletable -> wrappedCompletable.andThen(Observable.just(None.getInstance())))
        .subscribe(sharingOperationComplete);
    
    view.observeRequiredShareCount()
        .observeOn(presentationScheduler)
        .map(optionalCount -> optionalCount.map(count -> count > 1))
        .subscribe(requiredShareCountExceeds1);
    
    view.observeTotalShareCount()
        .observeOn(presentationScheduler)
        .map(optionalCount -> optionalCount.map(count -> count > 1))
        .subscribe(totalCountExceeds1);
    
    view.observeSecretFilePath()
        .map(Optional::isPresent)
        .subscribe(secretFileIsSet);
    
    view.observeOutputDirectoryPath()
        .map(Optional::isPresent)
        .subscribe(outputDirectoryIsSet);
    
    view.observeCreateSharesRequests()
        .observeOn(presentationScheduler)
        .map(request -> {
          if (!inputPreconditionsPass.hasValue() || !inputModel.hasValue()) {
            return Optional.<InputModel>empty();
          }
          
          if (!inputPreconditionsPass.getValue()) {
            return Optional.<InputModel>empty();
          }
          
          return Optional.of(inputModel.getValue());
        })
        .compose(filterOptionalAndUnwrap())
        .flatMapSingle(model -> secretFileIsValid(model.getSecretFile())
            .map(valid -> valid ? Optional.of(model) : Optional.<InputModel>empty()))
        .compose(filterOptionalAndUnwrap())
        .flatMapSingle(model -> outputDirectoryIsValid(model.getOutputDirectory())
            .map(valid -> valid ? Optional.of(model) : Optional.<InputModel>empty()))
        .compose(filterOptionalAndUnwrap())
        .subscribe(validRequest);
  }
  
  private Boolean inputPreconditionsPass(
      final Optional<Boolean> requiredShareCountExceeds1,
      final Optional<Boolean> totalCountExceeds1,
      final Optional<Boolean> requiredShareDoesNotExceedTotalShareCount,
      final boolean secretFileIsSet,
      final boolean outputDirectoryIsSet) {
    
    return Observable
        .fromArray(
            requiredShareCountExceeds1.orElse(false),
            totalCountExceeds1.orElse(false),
            requiredShareDoesNotExceedTotalShareCount.orElse(false),
            secretFileIsSet,
            outputDirectoryIsSet)
        .reduce(true, (cumulative, current) -> cumulative && current)
        .blockingGet();
  }
  
  private InputModel createModel(
      final int requiredShareCount,
      final int totalShareCount,
      final String secretFilePath,
      final String outputDirectoryPath) {
    
    return InputModel
        .builder()
        .setRequiredShareCount(requiredShareCount)
        .setTotalShareCount(totalShareCount)
        .setSecretFile(new File(secretFilePath))
        .setOutputDirectory(new File(outputDirectoryPath))
        .build();
  }
  
  private Single<Boolean> secretFileIsValid(final File secretFile) {
    final Single<Boolean> fileExists = rxFiles
        .exists(secretFile)
        .observeOn(viewScheduler)
        .flatMap(exists -> exists ?
            Single.just(true) :
            view
                .showDismissibleError(SECRET_FILE_DOES_NOT_EXIST)
                .andThen(view.showShareCreationNotInProgress())
                .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> notTooLarge = rxFiles
        .sizeInBytes(secretFile)
        .map(size -> size <= cryptoConstants.getMaxFileSizeBytes())
        .observeOn(viewScheduler)
        .flatMap(isNotTooLarge -> isNotTooLarge ?
            Single.just(true) :
            view.showDismissibleError(SECRET_FILE_IS_TOO_LARGE)
                .andThen(view.showShareCreationNotInProgress())
                .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    return fileExists
        .flatMap(exists -> exists ?
            notTooLarge : Single.just(false))
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showShareCreationNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
  }
  
  private Single<Boolean> outputDirectoryIsValid(final File outputDirectory) {
    final Single<Boolean> outputDirectoryExists = rxFiles
        .exists(outputDirectory)
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showShareCreationNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler)
        .flatMap(exists -> exists ?
            Single.just(true) :
            rxFiles.createDirectory(outputDirectory).andThen(Single.just(true)))
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(OUTPUT_DIRECTORY_CANNOT_BE_CREATED)
            .andThen(view.showShareCreationNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> outputDirectoryContainsShares = persistenceOperations
        .directoryContainsShareFiles(outputDirectory)
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showShareCreationNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> outputDirectoryContainsRecoveryScheme = persistenceOperations
        .directoryContainsRecoverySchemeFiles(outputDirectory)
        .observeOn(viewScheduler)
        .onErrorResumeNext(error -> view
            .showDismissibleError(FILESYSTEM_ERROR)
            .andThen(view.showShareCreationNotInProgress())
            .andThen(Single.just(false)))
        .observeOn(presentationScheduler);
    
    final Single<Boolean> outputDirectoryIsClean = Single.zip(
        outputDirectoryContainsShares,
        outputDirectoryContainsRecoveryScheme,
        (containsShares, containsRecoveryScheme) -> !containsShares && !containsRecoveryScheme);
    
    return outputDirectoryExists
        .flatMap(exists -> exists ? outputDirectoryIsClean : Single.just(false))
        .observeOn(viewScheduler)
        .flatMap(isClean -> !isClean ?
            view
                .showDismissibleError(OUTPUT_DIRECTORY_IS_NOT_CLEAN)
                .andThen(view.showShareCreationNotInProgress())
                .andThen(Single.just(false)) :
            Single.just(true))
        .onErrorResumeNext(error -> view.showDismissibleError(FILESYSTEM_ERROR).andThen(Single.just(false)))
        .observeOn(presentationScheduler);
  }
  
  private Single<SharingResult> shareSecret(final InputModel model, final BigInteger secret) {
    final Observable<Share> shares = Single
        .just(CreationScheme
            .builder()
            .setRequiredShareCount(model.getRequiredShareCount())
            .setTotalShareCount(model.getTotalShareCount())
            .setPrime(cryptoConstants.getPrime())
            .build())
        .flatMapObservable(scheme -> rxShamir.createShares(secret, scheme));
    
    final Single<RecoveryScheme> recoveryScheme = Single
        .just(RecoveryScheme
            .builder()
            .setRequiredShareCount(model.getRequiredShareCount())
            .setPrime(cryptoConstants.getPrime())
            .build());
    
    return Single.zip(
        shares.collectInto(new HashSet<>(), Set::add),
        recoveryScheme,
        SharingResult::create);
  }
  
  @AutoValue
  protected static abstract class InputModel {
    public abstract int getRequiredShareCount();
    
    public abstract int getTotalShareCount();
    
    public abstract File getSecretFile();
    
    public abstract File getOutputDirectory();
    
    public static Builder builder() {
      return new AutoValue_CreationPresenter_InputModel.Builder();
    }
    
    @AutoValue.Builder
    public static abstract class Builder {
      public abstract Builder setRequiredShareCount(final int requiredShareCount);
      
      public abstract Builder setTotalShareCount(final int totalShareCount);
      
      public abstract Builder setSecretFile(final File inputFile);
      
      public abstract Builder setOutputDirectory(final File outputDirectory);
      
      public abstract InputModel build();
    }
  }
  
  @AutoValue
  protected static abstract class SharingResult {
    public abstract Set<Share> getShares();
    
    public abstract RecoveryScheme getRecoveryScheme();
    
    public static SharingResult create(final Set<Share> shares, final RecoveryScheme recoveryScheme) {
      return new AutoValue_CreationPresenter_SharingResult(shares, recoveryScheme);
    }
  }
}