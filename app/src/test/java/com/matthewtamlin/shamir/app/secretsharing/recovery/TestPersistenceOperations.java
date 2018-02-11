package com.matthewtamlin.shamir.app.secretsharing.recovery;

import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.files.RxMocks;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.DeserialisationException;
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

import static com.matthewtamlin.shamir.app.secretsharing.serialisation.RxMocks.createMockJsonRecoverySchemeSerialiser;
import static com.matthewtamlin.shamir.app.secretsharing.serialisation.RxMocks.createMockJsonShareSerialiser;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
  
  private File file;
  
  private File directory;
  
  @Before
  public void setup() {
    shareSerialiser = createMockJsonShareSerialiser();
    recoverySchemeSerialiser = createMockJsonRecoverySchemeSerialiser();
    rxFiles = RxMocks.createMockRxFiles();
    
    persistenceOperations = new PersistenceOperations(shareSerialiser, recoverySchemeSerialiser, rxFiles);
    
    file = new File("file");
    directory = new File("directory");
    
    when(rxFiles.exists(file)).thenReturn(Single.just(true));
    when(rxFiles.isFile(file)).thenReturn(Single.just(true));
    when(rxFiles.isDirectory(file)).thenReturn(Single.just(false));
    when(rxFiles.createNewFile(file)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(file)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.complete());
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(""));
    when(rxFiles.getFilesInDirectory(file)).thenReturn(Observable.error(new IOException()));
    
    when(rxFiles.exists(directory)).thenReturn(Single.just(true));
    when(rxFiles.isFile(directory)).thenReturn(Single.just(false));
    when(rxFiles.isDirectory(directory)).thenReturn(Single.just(true));
    when(rxFiles.createNewFile(directory)).thenReturn(Completable.error(new IOException()));
    when(rxFiles.createDirectory(directory)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(directory), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(directory), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(directory)).thenReturn(Observable.empty());
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
  public void testLoadShareFromFile_nullFile() {
    persistenceOperations.loadShareFromFile(null);
  }
  
  @Test
  public void testLoadShareFromFile_fileDoesNotExist() {
    when(rxFiles.exists(file)).thenReturn(Single.just(false));
    when(rxFiles.isFile(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.isDirectory(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.createNewFile(file)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(file)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(file)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .loadShareFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testLoadShareFromFile_fileIsActuallyADirectory() {
    persistenceOperations
        .loadShareFromFile(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testLoadShareFromFile_fileDoesNotContainAValidShare() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_SHARE));
    
    when(shareSerialiser.deserialise(SERIALISED_SHARE))
        .thenReturn(Single.error(new DeserialisationException()));
    
    persistenceOperations
        .loadShareFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(DeserialisationException.class);
  }
  
  @Test
  public void testLoadShareFromFile_readFails() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    
    persistenceOperations
        .loadShareFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testLoadShareFromFile_readSucceedsAndFileContainsAValidShare() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_SHARE));
    
    when(shareSerialiser.deserialise(SERIALISED_SHARE)).thenReturn(Single.just(SHARE));
    
    persistenceOperations
        .loadShareFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(SHARE);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testFileContainsShare_nullFile() {
    persistenceOperations.fileContainsShare(null);
  }
  
  @Test
  public void testFileContainsShare_fileDoesNotExist() {
    final File file = new File("file.txt");
    
    when(rxFiles.exists(file)).thenReturn(Single.just(false));
    when(rxFiles.isFile(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.isDirectory(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.createNewFile(file)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(file)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(file)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .fileContainsShare(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testFileContainsShare_fileIsActuallyADirectory() {
    persistenceOperations
        .fileContainsShare(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testFileContainsShare_readFails() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    
    persistenceOperations
        .fileContainsShare(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testFileContainsShare_fileDoesNotContainAValidShare() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_SHARE));
    
    when(shareSerialiser.isValidSerialisation(SERIALISED_SHARE)).thenReturn(Single.just(false));
    
    persistenceOperations
        .fileContainsShare(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testFileContainsShare_fileContainsAValidShare() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_SHARE));
    
    when(shareSerialiser.isValidSerialisation(SERIALISED_SHARE)).thenReturn(Single.just(true));
    
    persistenceOperations
        .fileContainsShare(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testLoadRecoverySchemeFromFile_nullFile() {
    persistenceOperations.loadRecoverySchemeFromFile(null);
  }
  
  @Test
  public void testLoadRecoverySchemeFromFile_fileDoesNotExist() {
    when(rxFiles.exists(file)).thenReturn(Single.just(false));
    when(rxFiles.isFile(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.isDirectory(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.createNewFile(file)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(file)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(file)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .loadRecoverySchemeFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testLoadRecoverySchemeFromFile_fileIsActuallyADirectory() {
    persistenceOperations
        .loadRecoverySchemeFromFile(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testLoadRecoverySchemeFromFile_fileDoesNotContainAValidRecoveryScheme() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
    
    when(recoverySchemeSerialiser.deserialise(SERIALISED_RECOVERY_SCHEME))
        .thenReturn(Single.error(new DeserialisationException()));
    
    persistenceOperations
        .loadRecoverySchemeFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(DeserialisationException.class);
  }
  
  @Test
  public void testLoadRecoverySchemeFromFile_readFails() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    
    persistenceOperations
        .loadRecoverySchemeFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testLoadRecoverySchemeFromFile_readSucceedsAndFileContainsAValidRecoveryScheme() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
    
    when(recoverySchemeSerialiser.deserialise(SERIALISED_RECOVERY_SCHEME)).thenReturn(Single.just(RECOVERY_SCHEME));
    
    persistenceOperations
        .loadRecoverySchemeFromFile(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(RECOVERY_SCHEME);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testFileContainsRecoveryScheme_nullFile() {
    persistenceOperations.fileContainsRecoveryScheme(null);
  }
  
  @Test
  public void testFileContainsRecoveryScheme_fileDoesNotExist() {
    final File file = new File("file.txt");
    
    when(rxFiles.exists(file)).thenReturn(Single.just(false));
    when(rxFiles.isFile(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.isDirectory(file)).thenReturn(Single.error(new IOException()));
    when(rxFiles.createNewFile(file)).thenReturn(Completable.complete());
    when(rxFiles.createDirectory(file)).thenReturn(Completable.complete());
    when(rxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.error(new IOException()));
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    when(rxFiles.getFilesInDirectory(file)).thenReturn(Observable.error(new IOException()));
    
    persistenceOperations
        .fileContainsRecoveryScheme(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testFileContainsRecoveryScheme_fileIsActuallyADirectory() {
    persistenceOperations
        .fileContainsRecoveryScheme(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class)
        .assertNoValues();
  }
  
  @Test
  public void testFileContainsRecoveryScheme_readFails() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.error(new IOException()));
    
    persistenceOperations
        .fileContainsRecoveryScheme(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IOException.class);
  }
  
  @Test
  public void testFileContainsRecoveryScheme_fileDoesNotContainAValidRecoveryScheme() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
    
    when(recoverySchemeSerialiser.isValidSerialisation(SERIALISED_RECOVERY_SCHEME)).thenReturn(Single.just(false));
    
    persistenceOperations
        .fileContainsRecoveryScheme(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testFileContainsRecoveryScheme_fileContainsAValidRecoveryScheme() {
    when(rxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(SERIALISED_RECOVERY_SCHEME));
    
    when(recoverySchemeSerialiser.isValidSerialisation(SERIALISED_RECOVERY_SCHEME)).thenReturn(Single.just(true));
    
    persistenceOperations
        .fileContainsRecoveryScheme(file)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
  
  @Test
  public void testDefineNewRecoveredShareFile_nullDirectory() {
    persistenceOperations.defineNewRecoveredSecretFile(null);
  }
  
  @Test
  public void testDefineNewRecoveredShareFile_nonNullDirectory() {
    final File directory = new File("test");
    
    persistenceOperations
        .defineNewRecoveredSecretFile(directory)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(
            file -> file.getParentFile().equals(directory) && file.getName().equals("recovered-secret"));
  }
}