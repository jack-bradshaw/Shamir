package com.matthewtamlin.shamir.framework;

import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;

public class MockitoHelper {
  public static VerificationMode once() {
    return times(1);
  }
}