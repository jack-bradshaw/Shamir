package com.matthewtamlin.shamir.app.secretsharing.recovery;

import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.rxutilities.None;
import com.matthewtamlin.shamir.app.secretsharing.encoding.SecretEncoder;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static com.matthewtamlin.shamir.app.files.RxMocks.createMockRxFiles;
import static com.matthewtamlin.shamir.app.secretsharing.encoding.RxMocks.createMockSecretEncoder;
import static com.matthewtamlin.shamir.app.secretsharing.recovery.RxMocks.createMockPersistenceOperations;
import static com.matthewtamlin.shamir.app.secretsharing.recovery.RxMocks.createMockRecoveryView;
import static com.matthewtamlin.shamir.framework.MockitoHelper.once;
import static com.matthewtamlin.shamir.reactivejavaapi.RxMocks.createMockRxShamir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@SuppressWarnings("ConstantConditions")
public class TestRecoveryPresenter {
  @Rule
  public final TemporaryFolder temporaryFolder = new TemporaryFolder();
  
  private PublishSubject<Optional<? extends Set<String>>> shareFilePathsObservable;
  
  private PublishSubject<Optional<String>> recoverySchemeFilePathObservable;
  
  private PublishSubject<Optional<String>> outputDirectoryPathObservable;
  
  private PublishSubject<None> recoverSecretRequestObservable;
  
  private RecoveryPresenter presenter;
  
  private RecoveryView mockView;
  
  private RxShamir mockRxShamir;
  
  private SecretEncoder mockSecretEncoder;
  
  private PersistenceOperations mockPersistenceOperations;
  
  private RxFiles mockRxFiles;
  
  private Set<File> shareFiles;
  
  private File recoverySchemeFile;
  
  private File recoveredSecretFile;
  
  private File outputDirectory;
  
  private Map<File, byte[]> rawShares;
  
  private Map<File, String> stringShares;
  
  private Map<File, Share> shares;
  
  private byte[] rawRecoveryScheme;
  
  private String stringRecoveryScheme;
  
  private RecoveryScheme recoveryScheme;
  
  private byte[] rawRecoveredSecret;
  
  private BigInteger recoveredSecret;
  
  @Before
  public void setup() throws IOException {
    shareFilePathsObservable = PublishSubject.create();
    recoverySchemeFilePathObservable = PublishSubject.create();
    outputDirectoryPathObservable = PublishSubject.create();
    recoverSecretRequestObservable = PublishSubject.create();
    
    mockView = createMockRecoveryView();
    mockRxShamir = createMockRxShamir();
    mockSecretEncoder = createMockSecretEncoder();
    mockPersistenceOperations = createMockPersistenceOperations();
    mockRxFiles = createMockRxFiles();
    
    when(mockView.observeShareFilePaths()).thenReturn(shareFilePathsObservable);
    when(mockView.observeRecoverySchemeFilePath()).thenReturn(recoverySchemeFilePathObservable);
    when(mockView.observeOutputDirectoryPath()).thenReturn(outputDirectoryPathObservable);
    when(mockView.observeRecoverSharesRequests()).thenReturn(recoverSecretRequestObservable);
    
    when(mockView.showDismissibleError(any())).thenReturn(Completable.complete());
    when(mockView.enableRecoverSecretRequests()).thenReturn(Completable.complete());
    when(mockView.disableRecoverSecretRequests()).thenReturn(Completable.complete());
    
    presenter = new RecoveryPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
    
    shareFiles = new HashSet<>();
    rawShares = new HashMap<>();
    stringShares = new HashMap<>();
    shares = new HashMap<>();
    
    for (int i = 1; i < 4; i++) {
      final File file = temporaryFolder.newFile("share-" + i);
      
      shareFiles.add(file);
      rawShares.put(file, new byte[]{(byte) i});
      stringShares.put(file, "share-" + i);
      shares.put(file, Share.builder().setIndex(i).setValue(i).build());
    }
    
    recoverySchemeFile = temporaryFolder.newFile("recovery-scheme");
    rawRecoveryScheme = new byte[]{1};
    stringRecoveryScheme = "recovery";
    recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(3)
        .setPrime(11)
        .build();
    
    
    recoveredSecretFile = temporaryFolder.newFile("recovered-secret");
    rawRecoveredSecret = new byte[]{1};
    recoveredSecret = BigInteger.ONE;
    
    outputDirectory = temporaryFolder.newFolder("output");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullView() {
    new RecoveryPresenter(
        null,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullPresentationScheduler() {
    new RecoveryPresenter(
        mockView,
        null,
        Schedulers.trampoline(),
        mockRxShamir,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullViewScheduler() {
    new RecoveryPresenter(
        mockView,
        Schedulers.trampoline(),
        null,
        mockRxShamir,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRxShamir() {
    new RecoveryPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        null,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullSecretEncoder() {
    new RecoveryPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        null,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullFileHelper() {
    new RecoveryPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockSecretEncoder,
        null,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRxFiles() {
    new RecoveryPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockSecretEncoder,
        mockPersistenceOperations,
        null);
  }
  
  @Test
  public void testStartPresenting_calledBeforeStarting() {
    presenter.startPresenting().blockingGet();
  }
  
  @Test
  public void testStartPresenting_calledWhileStarted() {
    presenter.startPresenting().blockingGet();
    presenter.startPresenting().blockingGet();
  }
  
  @Test
  public void testStartPresenting_calledWhileStopped() {
    presenter.startPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
    presenter.startPresenting().blockingGet();
  }
  
  @Test
  public void testStopPresenting_calledBeforeStarting() {
    presenter.stopPresenting().blockingGet();
  }
  
  @Test
  public void testStopPresenting_calledWhileStarted() {
    presenter.startPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
  }
  
  @Test
  public void testStopPresenting_calledWhileStopped() {
    presenter.startPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
  }
  
  @Test
  public void testEventResponse_shareFilePathsChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    shareFilePathsObservable.onNext(Optional.empty());
    
    verify(mockView, never()).enableRecoverSecretRequests();
    verify(mockView, once()).disableRecoverSecretRequests();
    
    verify(mockView, never()).enableClearSelectedShareFilesButton();
    verify(mockView, once()).disableClearSelectedShareFilesButton();
    
    verify(mockView, never()).showSelectedShareCount(anyInt());
    verify(mockView, once()).hideSelectedShareCount();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_shareFilePathsChanged_setToMultipleFiles() {
    presenter.startPresenting().blockingGet();
    
    final Set<String> shareFilePaths = Observable
        .fromIterable(shareFiles)
        .map(File::getAbsolutePath)
        .collectInto(new HashSet<String>(), Set::add)
        .blockingGet();
    
    shareFilePathsObservable.onNext(Optional.of(shareFilePaths));
    
    verify(mockView, never()).disableRecoverSecretRequests();
    
    verify(mockView, once()).enableClearSelectedShareFilesButton();
    verify(mockView, never()).disableClearSelectedShareFilesButton();
  
    verify(mockView, once()).showSelectedShareCount(shareFiles.size());
    verify(mockView, never()).hideSelectedShareCount();
  
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverySchemeFilePathsChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    recoverySchemeFilePathObservable.onNext(Optional.empty());
    
    verify(mockView, never()).enableRecoverSecretRequests();
    verify(mockView, once()).disableRecoverSecretRequests();
    
    verify(mockView, never()).enableClearSelectedRecoverySchemeFileButton();
    verify(mockView, once()).disableClearRecoverySchemeFileButton();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverySchemeFilePathsChanged_setToNonEmpty() {
    presenter.startPresenting().blockingGet();
    
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    
    verify(mockView, never()).disableRecoverSecretRequests();
    
    verify(mockView, once()).enableClearSelectedRecoverySchemeFileButton();
    verify(mockView, never()).disableClearRecoverySchemeFileButton();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_outputDirectoryFilePathsChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    outputDirectoryPathObservable.onNext(Optional.empty());
    
    verify(mockView, never()).enableRecoverSecretRequests();
    verify(mockView, once()).disableRecoverSecretRequests();
    
    verify(mockView, never()).enableClearSelectedOutputDirectoryButton();
    verify(mockView, once()).disableClearSelectedOutputDirectoryButton();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_outputDirectoryFilePathsChanged_setToNonEmpty() {
    presenter.startPresenting().blockingGet();
    
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    verify(mockView, never()).disableRecoverSecretRequests();
    
    verify(mockView, once()).enableClearSelectedOutputDirectoryButton();
    verify(mockView, never()).disableClearSelectedOutputDirectoryButton();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsSet_shareFilePathsSetLast() {
    presenter.startPresenting().blockingGet();
    
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    shareFilePathsObservable.onNext(Optional.of(getShareFilePaths()));
    
    verify(mockView, once()).enableRecoverSecretRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsSet_recoverySchemeFilePathSetLast() {
    presenter.startPresenting().blockingGet();
    
    shareFilePathsObservable.onNext(Optional.of(getShareFilePaths()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    
    verify(mockView, once()).enableRecoverSecretRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsSet_outputDirectoryPathSetLast() {
    presenter.startPresenting().blockingGet();
    
    shareFilePathsObservable.onNext(Optional.of(getShareFilePaths()));
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    verify(mockView, once()).enableRecoverSecretRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_shareFilePathsSetToEmptyAndAllOtherInputsValid() {
    presenter.startPresenting().blockingGet();
    
    shareFilePathsObservable.onNext(Optional.empty());
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushRecoverSecretRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_recoverySchemeFilePathSetToEmptyAndAllOtherInputsValid() {
    presenter.startPresenting().blockingGet();
    
    shareFilePathsObservable.onNext(Optional.of(getShareFilePaths()));
    recoverySchemeFilePathObservable.onNext(Optional.empty());
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushRecoverSecretRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_outputDirectoryPathsSetToEmptyAndAllOtherInputsValid() {
    presenter.startPresenting().blockingGet();
    
    shareFilePathsObservable.onNext(Optional.of(getShareFilePaths()));
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.empty());
    
    pushRecoverSecretRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_anInputWhichWasPreviouslyValidIsCurrentlyInvalid() {
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    
    shareFilePathsObservable.onNext(Optional.empty());
    
    pushRecoverSecretRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_perfectScenario() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, never()).showDismissibleError(any());
    
    verifyRecoveredSecretWasPersisted();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotCheckIfShareFileExists() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(shareFiles.iterator().next())).thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_shareFileDoesNotExist() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(shareFiles.iterator().next())).thenReturn(Single.just(false));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.SHARE_FILE_DOES_NOT_EXIST);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotCheckIfRecoverySchemeFileExists() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(recoverySchemeFile)).thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_recoverySchemeFileDoesNotExist() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(recoverySchemeFile)).thenReturn(Single.just(false));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.RECOVERY_SCHEME_FILE_DOES_NOT_EXIST);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotCheckIfOutputDirectoryExists() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, atLeastOnce()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_outputDirectoryDoesNotExistAndCannotBeCreated() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.just(false));
    when(mockRxFiles.createDirectory(outputDirectory)).thenReturn(Completable.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, atLeastOnce()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.OUTPUT_DIRECTORY_CANNOT_BE_CREATED);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_outputDirectoryDoesNotExistAndCanBeCreated() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.just(false));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verifyRecoveredSecretWasPersisted();
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotCheckIfOutputDirectoryAlreadyContainsASecret() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockPersistenceOperations.directoryContainsRecoveredSecretFile(outputDirectory))
        .thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, atLeastOnce()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_outputDirectoryAlreadyContainsASecret() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockPersistenceOperations.directoryContainsRecoveredSecretFile(outputDirectory))
        .thenReturn(Single.just(true));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.OUTPUT_DIRECTORY_IS_NOT_CLEAN);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotReadFromShareFile() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    for (final File shareFile : shareFiles) {
      when(mockRxFiles.readStringFromFile(eq(shareFile), any())).thenReturn(Single.error(new IOException()));
      when(mockRxFiles.readBytesFromFile(eq(shareFile))).thenReturn(Single.error(new IOException()));
      when(mockPersistenceOperations.loadShareFromFile(shareFile)).thenReturn(Single.error(new IOException()));
    }
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotReadFromRecoverySchemeFile() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.readStringFromFile(eq(recoverySchemeFile), any())).thenReturn(Single.error(new IOException()));
    when(mockRxFiles.readBytesFromFile(eq(recoverySchemeFile))).thenReturn(Single.error(new IOException()));
    when(mockPersistenceOperations.loadRecoverySchemeFromFile(recoverySchemeFile))
        .thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotCheckIfFileContainsShare() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    for (final File shareFile : shareFiles) {
      when(mockRxFiles.readStringFromFile(eq(shareFile), any())).thenReturn(Single.error(new IOException()));
      when(mockRxFiles.readBytesFromFile(eq(shareFile))).thenReturn(Single.error(new IOException()));
      when(mockPersistenceOperations.fileContainsShare(shareFile)).thenReturn(Single.error(new IOException()));
    }
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_cannotCheckIfFileContainsRecoveryScheme() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.readStringFromFile(eq(recoverySchemeFile), any())).thenReturn(Single.error(new IOException()));
    when(mockRxFiles.readBytesFromFile(eq(recoverySchemeFile))).thenReturn(Single.error(new IOException()));
    when(mockPersistenceOperations.fileContainsRecoveryScheme(recoverySchemeFile))
        .thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_shareFileIsMalformed() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    for (final File shareFile : shareFiles) {
      when(mockPersistenceOperations.fileContainsShare(shareFile)).thenReturn(Single.just(false));
    }
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.SHARE_FILE_IS_MALFORMED);
  }
  
  @Test
  public void testEventResponse_recoverSecretRequested_recoverySchemeFileIsMalformed() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockPersistenceOperations.fileContainsRecoveryScheme(recoverySchemeFile)).thenReturn(Single.just(false));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.RECOVERY_SCHEME_IS_MALFORMED);
  }
  
  @Test
  public void testEventResponse_cannotCreateRecoveredSecretFile() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.createNewFile(recoveredSecretFile)).thenReturn(Completable.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, once()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.CANNOT_CREATE_RECOVERED_SECRET_FILE);
  }
  
  @Test
  public void testEventResponse_cannotWriteToRecoveredSecretFile() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxFiles.writeBytesToFile(any(), eq(recoveredSecretFile)))
        .thenReturn(Completable.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, atLeastOnce()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.CANNOT_WRITE_TO_RECOVERED_SECRET_FILE);
  }
  
  @Test
  public void testEventResponse_recoveryOperationFails() {
    setupMocksForPerfectSecretRecoveryScenario();
    
    when(mockRxShamir.recoverSecret(any(), any())).thenReturn(Single.error(new RuntimeException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushRecoverSecretRequest();
    
    verify(mockView, once()).showRecoveryInProgress();
    verify(mockView, atLeastOnce()).showRecoveryNotInProgress();
    
    verify(mockView, once()).showDismissibleError(DismissibleError.RECOVERY_FAILED);
  }
  
  private void setupMocksForPerfectSecretRecoveryScenario() {
    for (final File file : shareFiles) {
      when(mockRxFiles.exists(file)).thenReturn(Single.just(true));
      when(mockRxFiles.isFile(file)).thenReturn(Single.just(true));
      when(mockRxFiles.isDirectory(file)).thenReturn(Single.just(false));
      when(mockRxFiles.createNewFile(file)).thenReturn(Completable.error(new IOException()));
      when(mockRxFiles.createDirectory(file)).thenReturn(Completable.error(new IOException()));
      when(mockRxFiles.getFilesInDirectory(file)).thenReturn(Observable.error(new IOException()));
      when(mockRxFiles.readBytesFromFile(file)).thenReturn(Single.just(rawShares.get(file)));
      when(mockRxFiles.readStringFromFile(eq(file), any())).thenReturn(Single.just(stringShares.get(file)));
      when(mockRxFiles.writeBytesToFile(any(), eq(file))).thenReturn(Completable.complete());
      when(mockRxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.complete());
      
      when(mockPersistenceOperations.fileContainsShare(file)).thenReturn(Single.just(true));
      when(mockPersistenceOperations.fileContainsRecoveryScheme(file)).thenReturn(Single.just(false));
      when(mockPersistenceOperations.loadShareFromFile(file)).thenReturn(Single.just(shares.get(file)));
      when(mockPersistenceOperations.loadRecoverySchemeFromFile(file))
          .thenReturn(Single.error(new IOException()));
    }
    
    when(mockRxFiles.exists(recoverySchemeFile)).thenReturn(Single.just(true));
    when(mockRxFiles.isFile(recoverySchemeFile)).thenReturn(Single.just(true));
    when(mockRxFiles.isDirectory(recoverySchemeFile)).thenReturn(Single.just(false));
    when(mockRxFiles.createNewFile(recoverySchemeFile)).thenReturn(Completable.error(new IOException()));
    when(mockRxFiles.createDirectory(recoverySchemeFile)).thenReturn(Completable.error(new IOException()));
    when(mockRxFiles.getFilesInDirectory(recoverySchemeFile)).thenReturn(Observable.error(new IOException()));
    when(mockRxFiles.readBytesFromFile(recoverySchemeFile)).thenReturn(Single.just(rawRecoveryScheme));
    when(mockRxFiles.readStringFromFile(eq(recoverySchemeFile), any()))
        .thenReturn(Single.just(stringRecoveryScheme));
    when(mockRxFiles.writeBytesToFile(any(), eq(recoverySchemeFile))).thenReturn(Completable.complete());
    when(mockRxFiles.writeStringToFile(any(), eq(recoverySchemeFile), any())).thenReturn(Completable.complete());
    
    when(mockPersistenceOperations.fileContainsShare(recoverySchemeFile)).thenReturn(Single.just(false));
    when(mockPersistenceOperations.fileContainsRecoveryScheme(recoverySchemeFile)).thenReturn(Single.just(true));
    when(mockPersistenceOperations.loadShareFromFile(recoverySchemeFile))
        .thenReturn(Single.error(new IOException()));
    when(mockPersistenceOperations.loadRecoverySchemeFromFile(recoverySchemeFile))
        .thenReturn(Single.just(recoveryScheme));
    
    when(mockRxFiles.createNewFile(recoveredSecretFile)).thenReturn(Completable.complete());
    when(mockRxFiles.createDirectory(recoveredSecretFile)).thenReturn(Completable.error(new IOException()));
    when(mockRxFiles.getFilesInDirectory(recoveredSecretFile)).thenReturn(Observable.error(new IOException()));
    when(mockRxFiles.writeBytesToFile(any(), eq(recoveredSecretFile))).thenReturn(Completable.complete());
    when(mockRxFiles.writeStringToFile(any(), eq(recoveredSecretFile), any())).thenReturn(Completable.complete());
    
    when(mockRxFiles.createDirectory(outputDirectory)).thenReturn(Completable.complete());
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.just(true));
    when(mockRxFiles.isFile(outputDirectory)).thenReturn(Single.just(false));
    when(mockRxFiles.isDirectory(outputDirectory)).thenReturn(Single.just(true));
    when(mockRxFiles.getFilesInDirectory(outputDirectory)).thenReturn(Observable.empty());
    when(mockPersistenceOperations.directoryContainsRecoveredSecretFile(outputDirectory))
        .thenReturn(Single.just(false));
    
    when(mockPersistenceOperations.defineNewRecoveredSecretFile(outputDirectory))
        .thenReturn(Single.just(recoveredSecretFile));
    
    when(mockSecretEncoder.decodeSecret(recoveredSecret)).thenReturn(Single.just(rawRecoveredSecret));
    
    when(mockRxShamir.recoverSecret(new HashSet<>(shares.values()), recoveryScheme))
        .thenReturn(Single.just(recoveredSecret));
  }
  
  private Set<String> getShareFilePaths() {
    return Observable
        .fromIterable(shareFiles)
        .map(File::getAbsolutePath)
        .collectInto(new HashSet<String>(), Set::add)
        .blockingGet();
  }
  
  private void verifyNoFilesystemInteractions() {
    verifyZeroInteractions(mockPersistenceOperations);
    verifyZeroInteractions(mockRxFiles);
  }
  
  private void verifyRecoveredSecretWasPersisted() {
    verify(mockPersistenceOperations, once()).defineNewRecoveredSecretFile(outputDirectory);
    verify(mockRxFiles, once()).writeBytesToFile(rawRecoveredSecret, recoveredSecretFile);
  }
  
  private void pushValidValuesToInputObservables() {
    shareFilePathsObservable.onNext(Optional.of(getShareFilePaths()));
    recoverySchemeFilePathObservable.onNext(Optional.of(recoverySchemeFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
  }
  
  private void pushRecoverSecretRequest() {
    recoverSecretRequestObservable.onNext(None.getInstance());
  }
}