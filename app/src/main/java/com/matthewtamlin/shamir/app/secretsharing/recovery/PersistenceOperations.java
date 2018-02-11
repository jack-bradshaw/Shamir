package com.matthewtamlin.shamir.app.secretsharing.recovery;

import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.secretsharing.CharsetConstants;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.RecoverySchemeSerialiser;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.ShareSerialiser;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.io.File;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

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
  public Single<Share> loadShareFromFile(@Nonnull final File file) {
    checkNotNull(file, "\'file\' cannot be null.");
    
    return rxFiles
        .readStringFromFile(file, CharsetConstants.SHARE_FILE_CHARSET)
        .flatMap(shareSerialiser::deserialise);
  }
  
  @Nonnull
  public Single<Boolean> fileContainsShare(@Nonnull final File file) {
    checkNotNull(file, "\'file\' cannot be null.");
    
    return rxFiles
        .readStringFromFile(file, CharsetConstants.SHARE_FILE_CHARSET)
        .flatMap(shareSerialiser::isValidSerialisation);
  }
  
  @Nonnull
  public Single<RecoveryScheme> loadRecoverySchemeFromFile(@Nonnull final File file) {
    checkNotNull(file, "\'directory\' cannot be null.");
    
    return rxFiles
        .readStringFromFile(file, CharsetConstants.RECOVERY_SCHEME_CHARSET)
        .flatMap(recoverySchemeSerialiser::deserialise);
  }
  
  @Nonnull
  public Single<Boolean> fileContainsRecoveryScheme(@Nonnull final File file) {
    checkNotNull(file, "\file\' must not be null.");
    
    return rxFiles
        .readStringFromFile(file, CharsetConstants.RECOVERY_SCHEME_CHARSET)
        .flatMap(recoverySchemeSerialiser::isValidSerialisation);
  }
  
  @Nonnull
  public Single<File> defineNewRecoveredSecretFile(@Nonnull final File directory) {
    return Single.just(new File(directory, "recovered-secret"));
  }
  
  @Nonnull
  public Single<Boolean> directoryContainsRecoveredSecretFile(@Nonnull final File directory) {
    return rxFiles
        .getFilesInDirectory(directory)
        .filter(file -> file.getName().equals("recovered-secret"))
        .count()
        .map(count -> count > 0);
  }
}