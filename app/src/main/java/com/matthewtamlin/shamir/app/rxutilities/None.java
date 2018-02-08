package com.matthewtamlin.shamir.app.rxutilities;

import javax.annotation.Nonnull;

/**
 * Singleton for use in Observables which represent events with no data. Effectively an instantiable version of
 * {@link Void}.
 */
public class None {
  private static None ourInstance = new None();
  
  @Nonnull
  public static None getInstance() {
    return ourInstance;
  }
  
  private None() {}
}