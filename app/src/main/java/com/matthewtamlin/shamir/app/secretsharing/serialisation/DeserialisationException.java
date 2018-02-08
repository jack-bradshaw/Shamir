package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import javax.annotation.Nullable;

/**
 * Exception to indicate that a deserialisation operation failed due to malformed or invalid data.
 */
public class DeserialisationException extends Exception {
  public DeserialisationException() {
    super();
  }
  
  public DeserialisationException(@Nullable final String message) {
    super(message);
  }
  
  public DeserialisationException(@Nullable final String message, @Nullable final Throwable cause) {
    super(message, cause);
  }
  
  public DeserialisationException(@Nullable final Throwable cause) {
    super(cause);
  }
  
  protected DeserialisationException(
      @Nullable final String message,
      @Nullable final Throwable cause,
      final boolean enableSuppression,
      final boolean writableStackTrace) {
    
    super(message, cause, enableSuppression, writableStackTrace);
  }
}