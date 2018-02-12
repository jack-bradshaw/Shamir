/*
 * Copyright 2018 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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