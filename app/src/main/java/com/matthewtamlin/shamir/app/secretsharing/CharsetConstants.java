package com.matthewtamlin.shamir.app.secretsharing;

import java.nio.charset.Charset;

public final class CharsetConstants {
  public static final Charset SHARE_FILE_CHARSET = Charset.forName("UTF-8");
  
  public static final Charset RECOVERY_SCHEME_CHARSET = Charset.forName("UTF-8");
  
  private CharsetConstants() {
    throw new RuntimeException("Constants class. Do not instantiate.");
  }
}