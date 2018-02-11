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
    when(rxFiles.readStringFromFile(any(), any())).thenReturn(Single.never());
    when(rxFiles.writeStringToFile(any(), any(), any())).thenReturn(Completable.never());
    when(rxFiles.getFilesInDirectory(any())).thenReturn(Observable.never());
    
    return rxFiles;
  }
}