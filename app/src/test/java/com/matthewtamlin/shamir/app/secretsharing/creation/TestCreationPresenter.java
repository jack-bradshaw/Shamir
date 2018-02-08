package com.matthewtamlin.shamir.app.secretsharing.creation;

import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.rxutilities.None;
import com.matthewtamlin.shamir.app.secretsharing.encoding.SecretEncoder;
import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.commonslibrary.util.Pair;
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
import static com.matthewtamlin.shamir.app.secretsharing.creation.DismissibleError.*;
import static com.matthewtamlin.shamir.app.secretsharing.creation.PersistentError.*;
import static com.matthewtamlin.shamir.app.secretsharing.creation.RxMocks.*;
import static com.matthewtamlin.shamir.app.secretsharing.encoding.RxMocks.createMockSecretEncoder;
import static com.matthewtamlin.shamir.framework.MockitoHelper.once;
import static com.matthewtamlin.shamir.reactivejavaapi.RxMocks.createMockRxShamir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings({"ConstantConditions", "ThrowableNotThrown"})
public class TestCreationPresenter {
  @Rule
  public final TemporaryFolder temporaryFolder = new TemporaryFolder();
  
  private PublishSubject<Optional<Integer>> requiredShareCountObservable;
  
  private PublishSubject<Optional<Integer>> totalShareCountObservable;
  
  private PublishSubject<Optional<String>> secretFilePathObservable;
  
  private PublishSubject<Optional<String>> outputDirectoryPathObservable;
  
  private PublishSubject<None> createSharesRequests;
  
  private CreationPresenter presenter;
  
  private CreationView mockView;
  
  private RxShamir mockRxShamir;
  
  private CryptoConstants mockCryptoConstants;
  
  private SecretEncoder mockSecretEncoder;
  
  private PersistenceOperations mockPersistenceOperations;
  
  private RxFiles mockRxFiles;
  
  private File secretFile;
  
  private File outputDirectory;
  
  private byte[] rawSecret;
  
  private BigInteger encodedSecret;
  
  private BigInteger prime;
  
  private CreationScheme creationScheme;
  
  private RecoveryScheme recoveryScheme;
  
  private Set<Share> shares;
  
  private Map<Share, File> shareFiles;
  
  private File recoverySchemeFile;
  
  @Before
  public void setup() throws IOException {
    requiredShareCountObservable = PublishSubject.create();
    totalShareCountObservable = PublishSubject.create();
    secretFilePathObservable = PublishSubject.create();
    outputDirectoryPathObservable = PublishSubject.create();
    createSharesRequests = PublishSubject.create();
    
    mockView = createMockCreationView();
    mockRxShamir = createMockRxShamir();
    mockCryptoConstants = createMockCryptoConstants();
    mockSecretEncoder = createMockSecretEncoder();
    mockPersistenceOperations = createMockPersistenceOperations();
    mockRxFiles = createMockRxFiles();
    
    when(mockView.observeRequiredShareCount()).thenReturn(requiredShareCountObservable);
    when(mockView.observeTotalShareCount()).thenReturn(totalShareCountObservable);
    when(mockView.observeSecretFilePath()).thenReturn(secretFilePathObservable);
    when(mockView.observeOutputDirectoryPath()).thenReturn(outputDirectoryPathObservable);
    when(mockView.observeCreateSharesRequests()).thenReturn(createSharesRequests);
    
    when(mockView.showPersistentError(any())).thenReturn(Completable.complete());
    when(mockView.hidePersistentError(any())).thenReturn(Completable.complete());
    when(mockView.showDismissibleError(any())).thenReturn(Completable.complete());
    when(mockView.enableCreateSharesRequests()).thenReturn(Completable.complete());
    when(mockView.disableCreateSharesRequests()).thenReturn(Completable.complete());
    
    presenter = new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockCryptoConstants,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
    
    secretFile = temporaryFolder.newFile("input.txt");
    outputDirectory = temporaryFolder.newFolder("outputdir");
    
    prime = BigInteger.valueOf(59287586649473627L);
    
    rawSecret = new byte[]{0, 1, 127};
    encodedSecret = BigInteger.valueOf(1234);
    
    creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(prime)
        .build();
    
    recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(prime)
        .build();
    
    shares = Observable
        .range(1, creationScheme.getTotalShareCount() + 1)
        .map(value -> Share.builder().setIndex(value).setValue(value).build())
        .collectInto(new HashSet<Share>(), HashSet::add)
        .blockingGet();
    
    shareFiles = Observable
        .fromIterable(shares)
        .map(share -> Pair.create(share, new File(outputDirectory, "share-" + share.getIndex())))
        .collectInto(
            new HashMap<Share, File>(),
            (map, shareAndFile) -> map.put(shareAndFile.getKey(), shareAndFile.getValue()))
        .blockingGet();
    
    recoverySchemeFile = new File(outputDirectory, "recovery");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullView() {
    new CreationPresenter(
        null,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockCryptoConstants,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullPresentationScheduler() {
    new CreationPresenter(
        mockView,
        null,
        Schedulers.trampoline(),
        mockRxShamir,
        mockCryptoConstants,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullViewScheduler() {
    new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        null,
        mockRxShamir,
        mockCryptoConstants,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRxShamir() {
    new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        null,
        mockCryptoConstants,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullPrimeProvider() {
    new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        null,
        mockSecretEncoder,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullSecretEncoder() {
    new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockCryptoConstants,
        null,
        mockPersistenceOperations,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullPersistenceOperations() {
    new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockCryptoConstants,
        mockSecretEncoder,
        null,
        mockRxFiles);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRxFiles() {
    new CreationPresenter(
        mockView,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        mockRxShamir,
        mockCryptoConstants,
        mockSecretEncoder,
        mockPersistenceOperations,
        null);
  }
  
  @Test
  public void testStartPresenting_calledBeforeStarting() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(-1));
    
    verify(mockView, once()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testStartPresenting_calledWhileStarted() {
    presenter.startPresenting().blockingGet();
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(-1));
    
    verify(mockView, once()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testStartPresenting_calledWhileStopped() {
    presenter.startPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(-1));
    
    verify(mockView, once()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testStopPresenting_calledBeforeStarting() {
    presenter.stopPresenting().blockingGet();
    
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testStopPresenting_calledWhileStarted() {
    presenter.startPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(-1));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testStopPresenting_calledWhileStopped() {
    presenter.startPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
    presenter.stopPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(-1));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.empty());
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToOne() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(1));
    
    verify(mockView, once()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToTwo() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToThree() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(3));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToLessThanTotalShareCount() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(3));
    requiredShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToEqualTotalShareCount() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(3));
    requiredShareCountObservable.onNext(Optional.of(3));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToGreaterThanTotalShareCount() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(3));
    requiredShareCountObservable.onNext(Optional.of(4));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, once()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_requiredShareCountChanged_setToTwoWhileTotalShareCountIsEmpty() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.empty());
    requiredShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.empty());
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToOne() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(1));
    
    verify(mockView, once()).showPersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToTwo() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToThree() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(3));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_TOTAL_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToLessThanRequestedShareCount() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(3));
    totalShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, once()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, never()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToEqualRequiredShareCount() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(3));
    totalShareCountObservable.onNext(Optional.of(3));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToGreaterThanRequestedShareCount() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(3));
    totalShareCountObservable.onNext(Optional.of(4));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_totalShareCountChanged_setToTwoWhileRequiredShareCountIsEmpty() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.empty());
    totalShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, never()).showPersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_THAN_TWO_REQUIRED_SHARES);
    
    verify(mockView, never()).showPersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    verify(mockView, once()).hidePersistentError(FEWER_TOTAL_SHARES_THAN_REQUIRED_SHARES);
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_secretFilePathChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    secretFilePathObservable.onNext(Optional.empty());
    
    verify(mockView, never()).enableClearSelectedSecretFileButton();
    verify(mockView, once()).disableClearSelectedSecretFileButton();
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_secretFilePathChanged_setToNonEmpty() {
    presenter.startPresenting().blockingGet();
    
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    
    verify(mockView, once()).enableClearSelectedSecretFileButton();
    verify(mockView, never()).disableClearSelectedSecretFileButton();
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_outputDirectoryPathChanged_setToEmpty() {
    presenter.startPresenting().blockingGet();
    
    outputDirectoryPathObservable.onNext(Optional.empty());
    
    verify(mockView, never()).enableClearSelectedOutputDirectoryButton();
    verify(mockView, once()).disableClearSelectedOutputDirectoryButton();
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, once()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_outputDirectoryPathChanged_setToNonEmpty() {
    presenter.startPresenting().blockingGet();
    
    outputDirectoryPathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    
    verify(mockView, once()).enableClearSelectedOutputDirectoryButton();
    verify(mockView, never()).disableClearSelectedOutputDirectoryButton();
    
    verify(mockView, never()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsChangedToValidValues_requiredShareCountChangedLast() {
    presenter.startPresenting().blockingGet();
    
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    requiredShareCountObservable.onNext(Optional.of(2));
    
    verify(mockView, once()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsChangedToValidValues_totalShareCountChangedLast() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    totalShareCountObservable.onNext(Optional.of(3));
    
    verify(mockView, once()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsChangedToValidValues_inputFilePathChangedLast() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.of(3));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    
    verify(mockView, once()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_allInputsChangedToValidValues_outputDirectoryPathChangedLast() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    verify(mockView, once()).enableCreateSharesRequests();
    verify(mockView, never()).disableCreateSharesRequests();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_requiredShareCountSetToEmptyAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.empty());
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_requiredShareCountLessThanTwoAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(1));
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_fewerTotalSharesThanRequiredSharesAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(3));
    totalShareCountObservable.onNext(Optional.of(2));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_totalShareCountSetToEmptyAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.empty());
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_totalShareCountLessThanTwoAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.of(1));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_secretFileSetToEmptyAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.empty());
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_outputDirectorySetToEmptyAndAllOtherInputsAreValid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.empty());
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_anInputWhichWasPreviouslyValidIsCurrentlyInvalid() {
    presenter.startPresenting().blockingGet();
    
    requiredShareCountObservable.onNext(Optional.of(2));
    totalShareCountObservable.onNext(Optional.of(3));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
    
    requiredShareCountObservable.onNext(Optional.of(1));
    
    pushCreateSharesRequest();
    
    verifyNoFilesystemInteractions();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_perfectScenario() {
    setupMocksForPerfectShareCreationScenario();
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, once()).showShareCreationNotInProgress();
    
    verify(mockView, never()).showDismissibleError(any());
    
    verifySharesAndRecoverySchemeWerePersisted();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotCheckIfSecretFileExists() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.exists(secretFile)).thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, once()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_secretFileDoesNotExist() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.exists(secretFile)).thenReturn(Single.just(false));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, once()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(SECRET_FILE_DOES_NOT_EXIST);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_secretFileIsTooLarge() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.sizeInBytes(secretFile)).thenReturn(Single.just((long) prime.toByteArray().length));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, once()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(SECRET_FILE_IS_TOO_LARGE);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotCheckIfOutputDirectoryExists() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_outputDirectoryDoesNotExistAndCannotBeCreated() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.just(false));
    when(mockRxFiles.createDirectory(outputDirectory)).thenReturn(Completable.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(OUTPUT_DIRECTORY_CANNOT_BE_CREATED);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_outputDirectoryDoesNotExistButCanBeCreated() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.just(false));
    when(mockRxFiles.createDirectory(outputDirectory)).thenReturn(Completable.complete());
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, never()).showDismissibleError(any());
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verifySharesAndRecoverySchemeWerePersisted();
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotCheckIfOutputDirectoryAlreadyContainsShares() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockPersistenceOperations.directoryContainsShareFiles(any()))
        .thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotCheckIfOutputDirectoryAlreadyContainsRecoveryScheme() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockPersistenceOperations.directoryContainsRecoverySchemeFiles(any()))
        .thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_outputDirectoryAlreadyContainsShares() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockPersistenceOperations.directoryContainsShareFiles(any())).thenReturn(Single.just(true));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(OUTPUT_DIRECTORY_IS_NOT_CLEAN);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_outputDirectoryAlreadyContainsRecoveryScheme() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockPersistenceOperations.directoryContainsRecoverySchemeFiles(any())).thenReturn(Single.just(true));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(OUTPUT_DIRECTORY_IS_NOT_CLEAN);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotReadFromSecretFile() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.readStringFromFile(eq(secretFile), any())).thenReturn(Single.error(new IOException()));
    when(mockRxFiles.readBytesFromFile(secretFile)).thenReturn(Single.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(FILESYSTEM_ERROR);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotCreateShareFiles() {
    setupMocksForPerfectShareCreationScenario();
    
    for (final Share share : shares) {
      when(mockRxFiles.createNewFile(shareFiles.get(share))).thenReturn(Completable.error(new IOException()));
    }
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(CANNOT_CREATE_SHARE_FILE);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotCreateRecoverySchemeFile() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockRxFiles.createNewFile(recoverySchemeFile)).thenReturn(Completable.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(CANNOT_CREATE_RECOVERY_SCHEME_FILE);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotWriteToSharesFiles() {
    setupMocksForPerfectShareCreationScenario();
    
    for (final Share share : shares) {
      when(mockPersistenceOperations.saveShareToFile(share, shareFiles.get(share)))
          .thenReturn(Completable.error(new IOException()));
    }
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(CANNOT_WRITE_TO_SHARE_FILE);
  }
  
  @Test
  public void testEventResponse_createSharesRequested_cannotWriteToRecoverySchemeFile() {
    setupMocksForPerfectShareCreationScenario();
    
    when(mockPersistenceOperations.saveRecoverySchemeToFile(any(), any()))
        .thenReturn(Completable.error(new IOException()));
    
    presenter.startPresenting().blockingGet();
    
    pushValidValuesToInputObservables();
    pushCreateSharesRequest();
    
    verify(mockView, once()).showShareCreationInProgress();
    verify(mockView, atLeastOnce()).showShareCreationNotInProgress();
    
    verify(mockView, once()).showDismissibleError(CANNOT_WRITE_TO_RECOVERY_SCHEME_FILE);
  }
  
  private void setupMocksForPerfectShareCreationScenario() {
    when(mockRxFiles.createNewFile(eq(recoverySchemeFile))).thenReturn(Completable.complete());
    when(mockRxFiles.writeStringToFile(any(), eq(recoverySchemeFile), any())).thenReturn(Completable.complete());
    when(mockRxFiles.writeBytesToFile(any(), eq(recoverySchemeFile))).thenReturn(Completable.complete());
    when(mockPersistenceOperations.saveRecoverySchemeToFile(eq(recoveryScheme), eq(recoverySchemeFile)))
        .thenReturn(Completable.complete());
    when(mockPersistenceOperations.defineNewRecoverySchemeFile(outputDirectory))
        .thenReturn(Single.just(recoverySchemeFile));
    
    when(mockRxFiles.readStringFromFile(eq(secretFile), any())).thenReturn(Single.just(Arrays.toString(rawSecret)));
    when(mockRxFiles.readBytesFromFile(secretFile)).thenReturn(Single.just(rawSecret));
    when(mockRxFiles.exists(secretFile)).thenReturn(Single.just(true));
    when(mockRxFiles.isFile(secretFile)).thenReturn(Single.just(true));
    when(mockRxFiles.isDirectory(secretFile)).thenReturn(Single.just(false));
    when(mockRxFiles.sizeInBytes(secretFile)).thenReturn(Single.just((long) prime.toByteArray().length - 1));
    
    when(mockRxFiles.createDirectory(outputDirectory)).thenReturn(Completable.complete());
    when(mockRxFiles.exists(outputDirectory)).thenReturn(Single.just(true));
    when(mockRxFiles.isFile(outputDirectory)).thenReturn(Single.just(false));
    when(mockRxFiles.isDirectory(outputDirectory)).thenReturn(Single.just(true));
    when(mockRxFiles.getFilesInDirectory(outputDirectory)).thenReturn(Observable.empty());
    when(mockPersistenceOperations.directoryContainsShareFiles(outputDirectory)).thenReturn(Single.just(false));
    when(mockPersistenceOperations.directoryContainsRecoverySchemeFiles(outputDirectory)).thenReturn(Single.just(false));
    
    for (final Share share : shares) {
      final File file = shareFiles.get(share);
      
      when(mockRxFiles.createNewFile(file)).thenReturn(Completable.complete());
      when(mockRxFiles.isFile(file)).thenReturn(Single.just(true));
      when(mockRxFiles.isDirectory(file)).thenReturn(Single.just(false));
      when(mockRxFiles.writeStringToFile(any(), eq(file), any())).thenReturn(Completable.complete());
      when(mockRxFiles.writeBytesToFile(any(), eq(file))).thenReturn(Completable.complete());
      when(mockPersistenceOperations.defineNewShareFile(eq(share), any())).thenReturn(Single.just(file));
      when(mockPersistenceOperations.saveShareToFile(share, file)).thenReturn(Completable.complete());
    }
    
    when(mockSecretEncoder.encodeSecret(rawSecret)).thenReturn(Single.just(encodedSecret));
    
    when(mockCryptoConstants.getPrime()).thenReturn(prime);
    when(mockCryptoConstants.getMaxFileSizeBytes()).thenReturn(prime.toByteArray().length - 1);
    
    when(mockRxShamir.createShares(encodedSecret, creationScheme)).thenReturn(Observable.fromIterable(shares));
  }
  
  private void verifyNoFilesystemInteractions() {
    verifyZeroInteractions(mockPersistenceOperations);
    verifyZeroInteractions(mockRxFiles);
  }
  
  private void verifySharesAndRecoverySchemeWerePersisted() {
    for (final Share share : shares) {
      verify(mockPersistenceOperations, once()).defineNewShareFile(share, outputDirectory);
      verify(mockPersistenceOperations, once()).saveShareToFile(share, shareFiles.get(share));
    }
    
    verify(mockPersistenceOperations, once()).defineNewRecoverySchemeFile(outputDirectory);
    verify(mockPersistenceOperations, once()).saveRecoverySchemeToFile(recoveryScheme, recoverySchemeFile);
  }
  
  private void pushValidValuesToInputObservables() {
    requiredShareCountObservable.onNext(Optional.of(creationScheme.getRequiredShareCount()));
    totalShareCountObservable.onNext(Optional.of(creationScheme.getTotalShareCount()));
    secretFilePathObservable.onNext(Optional.of(secretFile.getAbsolutePath()));
    outputDirectoryPathObservable.onNext(Optional.of(outputDirectory.getAbsolutePath()));
  }
  
  private void pushCreateSharesRequest() {
    createSharesRequests.onNext(None.getInstance());
  }
}