package com.matthewtamlin.shamir.app.rxutilities;

import io.reactivex.ObservableTransformer;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Operators {
  @Nonnull
  public static <R> ObservableTransformer<Optional<? extends R>, R> filterOptionalAndUnwrap() {
    return upstream -> upstream.filter(Optional::isPresent).map(Optional::get);
  }
}