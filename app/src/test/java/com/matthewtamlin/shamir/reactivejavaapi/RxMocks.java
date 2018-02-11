package com.matthewtamlin.shamir.reactivejavaapi;

import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import io.reactivex.Observable;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxMocks {
  public static RxShamir createMockRxShamir() {
    final RxShamir rxShamir = mock(RxShamir.class);
    
    when(rxShamir.createShares(any(), any())).thenReturn(Observable.never());
    when(rxShamir.recoverSecret(any(), any())).thenReturn(Single.never());
    
    return rxShamir;
  }
}
