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


import com.google.common.collect.ImmutableList;
import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.RecoverySchemeSerialiser;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.ShareSerialiser;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.matthewtamlin.shamir.app.files.RxMocks.createMockRxFiles;
import static com.matthewtamlin.shamir.app.secretsharing.serialisation.RxMocks.createMockJsonRecoverySchemeSerialiser;
import static com.matthewtamlin.shamir.app.secretsharing.serialisation.RxMocks.createMockJsonShareSerialiser;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("ConstantConditions")
public class TestPersistenceOperations {
  private static final Share SHARE = Share
      .builder()
      .setIndex(1)
      .setValue(1)
      .build();
  
  private static final RecoveryScheme RECOVERY_SCHEME = RecoveryScheme
      .builder()
      .setRequiredShareCount(2)
      .setPrime(3)
      .build();
  
  private static final String SERIALISED_SHARE = "serialised share";
  
  private static final String SERIALISED_RECOVERY_SCHEME = "serialised recovery scheme";
  
  private ShareSerialiser shareSerialiser;
  
  private RecoverySchemeSerialiser recoverySchemeSerialiser;
  
  private RxFiles rxFiles;
  
  private PersistenceOperations persistenceOperations;
  
  @Before
  public void setup() {
    shareSerialiser = createMockJsonShareSerialiser();
    recoverySchemeSerialiser = createMockJsonRecoverySchemeSerialiser();
    rxFiles = createMockRxFiles();
    
    persistenceOperations = new PersistenceOperations(shareSerialiser, recoverySchemeSerialiser, rxFiles);
    
    when(shareSerialiser.serialise(SHARE)).thenReturn(Single.just(SERIALISED_SHARE));
    when(recoverySchemeSerialiser.serialise(RECOVERY_SCHEME)).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullShareSerialiser() {
    new PersistenceOperations(null, recoverySchemeSerialiser, rxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRecoverySchemeSerialiser() {
    new PersistenceOperations(shareSerialiser, null, rxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRxFiles() {
    new PersistenceOperations(shareSerialiser, recoverySchemeSerialiser, null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSaveShareToFile_nullShare() {
    persistenceOperations.saveShareToFile(null, new File("test"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSaveShareToFile_nullFile() {
    persistenceOperations.saveShareToFile(SHARE, null);
  }
  
  @Test
  public void testSaveShareToFile_fileDoesNotExist() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.empty());
    
    when(rxFiles.exists(child)).thenReturn(Single.just(false));
    when(rxFiles.isFile(child)).thenReturn(Single.error(new IOException()));
    when(rxFiles.isDirectory(child)).thenReturn(Single.error(new IOException()));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(child)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveShareToFile(SHARE, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testSaveShareToFile_fileIsActuallyADirectory() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.just(child));
    
    when(rxFiles.exists(child)).thenReturn(Single.just(true));
    when(rxFiles.isFile(child)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(child)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(child)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveShareToFile(SHARE, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testSaveShareToFile_writeFails() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.just(child));
    
    when(rxFiles.exists(child)).thenReturn(Single.just(true));
    when(rxFiles.isFile(child)).thenReturn(Single.just(true));
    when(rxFiles.isDirectory(child)).thenReturn(Single.just(false));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(child)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.just(""));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveShareToFile(SHARE, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
    
    verify(rxFiles, times(1)).writeStringToFile(SERIALISED_SHARE, child, Charset.defaultCharset());
  }
  
  @Test
  public void testSaveShareToFile_writeSuccessful() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.just(child));
    
    when(rxFiles.exists(child)).thenReturn(Single.just(true));
    when(rxFiles.isFile(child)).thenReturn(Single.just(true));
    when(rxFiles.isDirectory(child)).thenReturn(Single.just(false));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(child)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.complete());
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.just(""));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveShareToFile(SHARE, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    verify(rxFiles, times(1)).writeStringToFile(SERIALISED_SHARE, child, Charset.defaultCharset());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSaveRecoverySchemeToFile_nullScheme() {
    persistenceOperations.saveRecoverySchemeToFile(null, new File("test"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSaveRecoverySchemeToFile_nullFile() {
    persistenceOperations.saveRecoverySchemeToFile(RECOVERY_SCHEME, null);
  }
  
  @Test
  public void testSaveRecoverySchemeToFile_fileDoesNotExist() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.empty());
    
    when(rxFiles.exists(child)).thenReturn(Single.just(false));
    when(rxFiles.isFile(child)).thenReturn(Single.error(new IOException()));
    when(rxFiles.isDirectory(child)).thenReturn(Single.error(new IOException()));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(child)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveRecoverySchemeToFile(RECOVERY_SCHEME, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testSaveRecoverySchemeToFile_fileIsActuallyADirectory() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.just(child));
    
    when(rxFiles.exists(child)).thenReturn(Single.just(true));
    when(rxFiles.isFile(child)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(child)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(child)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    
    persistenceOperations
        .saveRecoverySchemeToFile(RECOVERY_SCHEME, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testSaveRecoverySchemeToFile_writeFails() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.just(child));
    
    when(rxFiles.exists(child)).thenReturn(Single.just(true));
    when(rxFiles.isFile(child)).thenReturn(Single.just(true));
    when(rxFiles.isDirectory(child)).thenReturn(Single.just(false));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(child)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveRecoverySchemeToFile(RECOVERY_SCHEME, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNotComplete();
    
    verify(rxFiles, times(1)).writeStringToFile(SERIALISED_RECOVERY_SCHEME, child, Charset.defaultCharset());
  }
  
  @Test
  public void testSaveRecoverySchemeToFile_writeSuccessful() {
    final File parent = new File("parent");
    final File child = new File(parent, "child.txt");
    
    when(rxFiles.exists(parent)).thenReturn(Single.just(true));
    when(rxFiles.isFile(parent)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(parent)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(parent)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(parent)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(parent), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(parent), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(parent)).thenReturn(Observable.just(child));
    
    when(rxFiles.exists(child)).thenReturn(Single.just(true));
    when(rxFiles.isFile(child)).thenReturn(Single.just(true));
    when(rxFiles.isDirectory(child)).thenReturn(Single.just(false));
    when(rxFiles.createNewFile(child)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(child)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(child), any())).thenReturn(Completable.complete());
    when(rxFiles.readStringFromFile(eq(child), any())).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
    when(rxFiles.getFilesInDirectory(child)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .saveRecoverySchemeToFile(RECOVERY_SCHEME, child)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
    
    verify(rxFiles, times(1)).writeStringToFile(SERIALISED_RECOVERY_SCHEME, child, Charset.defaultCharset());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDefineNewShareFile_nullShare() {
    persistenceOperations.defineNewShareFile(null, new File(""));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDefineNewShareFile_nullDirectory() {
    persistenceOperations.defineNewShareFile(mock(Share.class), null);
  }
  
  @Test
  public void testDefineNewShareFile_nonNullValues() {
    final Share share = Share
        .builder()
        .setIndex(20)
        .setValue(1)
        .build();
    
    final File directory = new File("test");
    
    persistenceOperations
        .defineNewShareFile(share, directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(file -> file.getParentFile().equals(directory) && file.getName().equals("share-20"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDefineNewRecoverySchemeFile_nullDirectory() {
    persistenceOperations.defineNewRecoverySchemeFile(null);
  }
  
  @Test
  public void testDefineNewRecoveryScheme_nonNullDirectory() {
    final File directory = new File("test");
    
    persistenceOperations
        .defineNewRecoverySchemeFile(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(file -> file.getParentFile().equals(directory) && file.getName().equals("recovery-scheme"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDirectoryContainsShare_nullDirectory() {
    persistenceOperations.directoryContainsShareFiles(null);
  }
  
  @Test
  public void testDirectoryContainsShareFile_directoryContainsNoShareFiles() {
    final File directory = new File("test");
    
    final List<File> files = ImmutableList.of(
        new File("shre-1"),
        new File("share-"),
        new File("share1"),
        new File("share-1a1"),
        new File("share--1"),
        new File("Share-1"),
        new File("share 1"),
        new File("SHARE-1"),
        new File("Share 1"),
        new File("-"),
        new File("-1"));
    
    when(rxFiles.getFilesInDirectory(directory)).thenReturn(Observable.fromIterable(files));
    
    persistenceOperations
        .directoryContainsShareFiles(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testDirectoryContainsShareFile_directoryContainsOneShareFile() {
    final File directory = new File("test");
    
    when(rxFiles.getFilesInDirectory(directory)).thenReturn(Observable.just(new File("share-1")));
    
    persistenceOperations
        .directoryContainsShareFiles(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test
  public void testDirectoryContainsShareFile_directoryContainsTwoShareFiles() {
    final File directory = new File("test");
    
    final List<File> files = ImmutableList.of(new File("share-1"), new File("share-2"));
    
    when(rxFiles.getFilesInDirectory(directory)).thenReturn(Observable.fromIterable(files));
    
    persistenceOperations
        .directoryContainsShareFiles(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDirectoryContainsRecoveryScheme_nullDirectory() {
    persistenceOperations.directoryContainsRecoverySchemeFiles(null);
  }
  
  @Test
  public void testDirectoryContainsRecoverySchemeFile_directoryDoesNotContainsRecoverySchemeFile() {
    final File directory = new File("test");
    
    final List<File> files = ImmutableList.of(
        new File("recover-scheme"),
        new File("recovery-schme"),
        new File("recovery"),
        new File("scheme"),
        new File("-"),
        new File("recovery--scheme"));
    
    when(rxFiles.getFilesInDirectory(directory)).thenReturn(Observable.fromIterable(files));
    
    persistenceOperations
        .directoryContainsRecoverySchemeFiles(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testDirectoryContainsRecoverySchemeFile_directoryContainsRecoverySchemeFile() {
    final File directory = new File("test");
    
    when(rxFiles.getFilesInDirectory(directory)).thenReturn(Observable.just(new File("recovery-scheme")));
    
    persistenceOperations
        .directoryContainsRecoverySchemeFiles(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
}