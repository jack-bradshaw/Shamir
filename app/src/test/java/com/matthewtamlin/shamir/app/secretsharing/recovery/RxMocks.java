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