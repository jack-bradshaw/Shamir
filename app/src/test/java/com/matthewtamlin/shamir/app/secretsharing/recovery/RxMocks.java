package com.matthewtamlin.shamir.app.secretsharing.recovery;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxMocks {
  public static RecoveryView createMockRecoveryView() {
    final RecoveryView recoveryView = mock(RecoveryView.class);
    
    when(recoveryView.observeShareFilePaths()).thenReturn(Observable.never());
    when(recoveryView.observeRecoverySchemeFilePath()).thenReturn(Observable.never());
    when(recoveryView.observeOutputDirectoryPath()).thenReturn(Observable.never());
    when(recoveryView.observeRecoverSharesRequests()).thenReturn(Observable.never());
    when(recoveryView.enableRecoverSecretRequests()).thenReturn(Completable.never());
    when(recoveryView.disableRecoverSecretRequests()).thenReturn(Completable.never());
    when(recoveryView.enableClearSelectedShareFilesButton()).thenReturn(Completable.never());
    when(recoveryView.disableClearSelectedShareFilesButton()).thenReturn(Completable.never());
    when(recoveryView.enableClearSelectedRecoverySchemeFileButton()).thenReturn(Completable.never());
    when(recoveryView.disableClearRecoverySchemeFileButton()).thenReturn(Completable.never());
    when(recoveryView.enableClearSelectedOutputDirectoryButton()).thenReturn(Completable.never());
    when(recoveryView.disableClearSelectedOutputDirectoryButton()).thenReturn(Completable.never());
    when(recoveryView.showRecoveryInProgress()).thenReturn(Completable.never());
    when(recoveryView.showRecoveryNotInProgress()).thenReturn(Completable.never());
    
    return recoveryView;
  }
  
  public static PersistenceOperations createMockPersistenceOperations() {
    final PersistenceOperations persistenceOperations = mock(PersistenceOperations.class);
    
    
    when(persistenceOperations.loadShareFromFile(any())).thenReturn(Single.never());
    when(persistenceOperations.loadRecoverySchemeFromFile(any())).thenReturn(Single.never());
    when(persistenceOperations.defineNewRecoveredSecretFile(any())).thenReturn(Single.never());
    when(persistenceOperations.fileContainsShare(any())).thenReturn(Single.never());
    when(persistenceOperations.fileContainsRecoveryScheme(any())).thenReturn(Single.never());
    when(persistenceOperations.directoryContainsRecoveredSecretFile(any())).thenReturn(Single.never());
    
    return persistenceOperations;
  }
}