package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxMocks {
  public static JsonRecoverySchemeSerialiser createMockJsonRecoverySchemeSerialiser() {
    final JsonRecoverySchemeSerialiser serialiser = mock(JsonRecoverySchemeSerialiser.class);
    
    when(serialiser.isValidSerialisation(any())).thenReturn(Single.never());
    when(serialiser.serialise(any())).thenReturn(Single.never());
    when(serialiser.deserialise(any())).thenReturn(Single.never());
    
    return serialiser;
  }
  
  public static JsonShareSerialiser createMockJsonShareSerialiser() {
    final JsonShareSerialiser serialiser = mock(JsonShareSerialiser.class);
    
    when(serialiser.isValidSerialisation(any())).thenReturn(Single.never());
    when(serialiser.serialise(any())).thenReturn(Single.never());
    when(serialiser.deserialise(any())).thenReturn(Single.never());
    
    return serialiser;
  }
}