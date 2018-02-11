package com.matthewtamlin.shamir.app.secretsharing.encoding;

import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxMocks {
  public static SecretEncoder createMockSecretEncoder() {
    final SecretEncoder secretEncoder = mock(SecretEncoder.class);
    
    when(secretEncoder.encodeSecret(any())).thenReturn(Single.never());
    when(secretEncoder.decodeSecret(any())).thenReturn(Single.never());
    
    return secretEncoder;
  }
}