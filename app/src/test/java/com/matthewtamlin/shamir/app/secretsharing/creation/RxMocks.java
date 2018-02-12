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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxMocks {
  public static CreationView createMockCreationView() {
    final CreationView creationView = mock(CreationView.class);
    
    when(creationView.observeRequiredShareCount()).thenReturn(Observable.never());
    when(creationView.observeTotalShareCount()).thenReturn(Observable.never());
    when(creationView.observeSecretFilePath()).thenReturn(Observable.never());
    when(creationView.observeOutputDirectoryPath()).thenReturn(Observable.never());
    when(creationView.observeCreateSharesRequests()).thenReturn(Observable.never());
    when(creationView.showPersistentError(any())).thenReturn(Completable.never());
    when(creationView.hidePersistentError(any())).thenReturn(Completable.never());
    when(creationView.enableCreateSharesRequests()).thenReturn(Completable.never());
    when(creationView.disableCreateSharesRequests()).thenReturn(Completable.never());
    when(creationView.enableClearSelectedSecretFileButton()).thenReturn(Completable.never());
    when(creationView.disableClearSelectedSecretFileButton()).thenReturn(Completable.never());
    when(creationView.enableClearSelectedOutputDirectoryButton()).thenReturn(Completable.never());
    when(creationView.disableClearSelectedOutputDirectoryButton()).thenReturn(Completable.never());
    when(creationView.showShareCreationInProgress()).thenReturn(Completable.never());
    when(creationView.showShareCreationNotInProgress()).thenReturn(Completable.never());
    
    return creationView;
  }
  
  public static PersistenceOperations createMockPersistenceOperations() {
    final PersistenceOperations persistenceOperations = mock(PersistenceOperations.class);
    
    when(persistenceOperations.saveShareToFile(any(), any())).thenReturn(Completable.never());
    when(persistenceOperations.saveRecoverySchemeToFile(any(), any())).thenReturn(Completable.never());
    when(persistenceOperations.directoryContainsShareFiles(any())).thenReturn(Single.never());
    when(persistenceOperations.directoryContainsRecoverySchemeFiles(any())).thenReturn(Single.never());
    when(persistenceOperations.defineNewShareFile(any(), any())).thenReturn(Single.never());
    when(persistenceOperations.defineNewRecoverySchemeFile(any())).thenReturn(Single.never());
    
    return persistenceOperations;
  }
  
  public static CryptoConstants createMockCryptoConstants() {
    final CryptoConstants cryptoConstants = mock(CryptoConstants.class);
    
    when(cryptoConstants.getMaxFileSizeBytes()).thenReturn(0);
    when(cryptoConstants.getPrime()).thenReturn(BigInteger.ZERO);
    
    return cryptoConstants;
  }
}