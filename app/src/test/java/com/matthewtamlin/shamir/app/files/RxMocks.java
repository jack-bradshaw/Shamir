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

package com.matthewtamlin.shamir.app.files;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxMocks {
  public static RxFiles createMockRxFiles() {
    final RxFiles rxFiles = mock(RxFiles.class);
    
    when(rxFiles.isDirectory(any())).thenReturn(Single.never());
    when(rxFiles.isFile(any())).thenReturn(Single.never());
    when(rxFiles.exists(any())).thenReturn(Single.never());
    when(rxFiles.createDirectory(any())).thenReturn(Completable.never());
    when(rxFiles.createNewFile(any())).thenReturn(Completable.never());
    when(rxFiles.delete(any())).thenReturn(Completable.complete());
    when(rxFiles.readStringFromFile(any(), any())).thenReturn(Single.never());
    when(rxFiles.writeStringToFile(any(), any(), any())).thenReturn(Completable.never());
    when(rxFiles.getFilesInDirectory(any())).thenReturn(Observable.never());
    
    return rxFiles;
  }
}