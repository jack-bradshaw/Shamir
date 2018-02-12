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

import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.secretsharing.CharsetConstants;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.RecoverySchemeSerialiser;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.ShareSerialiser;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.io.File;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.lang.String.format;

public class PersistenceOperations {
  private final ShareSerialiser shareSerialiser;
  
  private final RecoverySchemeSerialiser recoverySchemeSerialiser;
  
  private final RxFiles rxFiles;
  
  public PersistenceOperations(
      @Nonnull final ShareSerialiser shareSerialiser,
      @Nonnull final RecoverySchemeSerialiser recoverySchemeSerialiser,
      @Nonnull final RxFiles rxFiles) {
    
    this.shareSerialiser = checkNotNull(shareSerialiser, "\'shareSerialiser\' must not be null.");
    
    this.recoverySchemeSerialiser = checkNotNull(
        recoverySchemeSerialiser,
        "\'recoverySchemeSerialiser\' must not be null.");
    
    this.rxFiles = checkNotNull(rxFiles, "\'rxFiles\' must not be null.");
  }
  
  @Nonnull
  public Completable saveShareToFile(@Nonnull final Share share, @Nonnull final File file) {
    checkNotNull(share, "\'share\' must not be null.");
    checkNotNull(file, "\'file\' must not be null.");
    
    return shareSerialiser
        .serialise(share)
        .flatMapCompletable(serialisedShare -> rxFiles.writeStringToFile(
            serialisedShare, file,
            CharsetConstants.SHARE_FILE_CHARSET));
  }
  
  @Nonnull
  public Completable saveRecoverySchemeToFile(@Nonnull final RecoveryScheme recoveryScheme, @Nonnull final File file) {
    checkNotNull(recoveryScheme, "\'recoveryScheme\' must not be null.");
    checkNotNull(file, "\'file\' must not be null.");
    
    return recoverySchemeSerialiser
        .serialise(recoveryScheme)
        .flatMapCompletable(serialisedShare -> rxFiles.writeStringToFile(
            serialisedShare, file,
            CharsetConstants.RECOVERY_SCHEME_CHARSET));
  }
  
  @Nonnull
  public Single<File> defineNewShareFile(@Nonnull final Share share, @Nonnull final File directory) {
    checkNotNull(share, "\'share\' must not be null.");
    checkNotNull(directory, "\'directory\' must not be null.");
    
    return Single.just(new File(directory, format("share-%1$s", share.getIndex())));
  }
  
  @Nonnull
  public Single<File> defineNewRecoverySchemeFile(@Nonnull final File directory) {
    checkNotNull(directory, "\'directory\' must not be null.");
    
    return Single.just(new File(directory, "recovery-scheme"));
  }
  
  @Nonnull
  public Single<Boolean> directoryContainsShareFiles(@Nonnull final File directory) {
    checkNotNull(directory, "\'directory\' must not be null.");
    
    return rxFiles
        .getFilesInDirectory(directory)
        .map(File::getName)
        .map(name -> name.matches("share-\\d+"))
        .reduce(true, (noPreviousMatches, thisMatches) -> noPreviousMatches && !thisMatches)
        .map(noMatches -> !noMatches);
  }
  
  @Nonnull
  public Single<Boolean> directoryContainsRecoverySchemeFiles(@Nonnull final File directory) {
    checkNotNull(directory, "\'directory\' must not be null.");
    
    return rxFiles
        .getFilesInDirectory(directory)
        .map(File::getName)
        .map(name -> name.matches("recovery-scheme"))
        .reduce(true, (noPreviousMatches, thisMatches) -> noPreviousMatches && !thisMatches)
        .map(noMatches -> !noMatches);
  }
}