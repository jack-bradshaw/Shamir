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

package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Single;

import javax.annotation.Nonnull;

public interface ShareSerialiser {
  @Nonnull
  public Single<String> serialise(@Nonnull final Share share);
  
  @Nonnull
  public Single<Share> deserialise(@Nonnull final String serialisedShare);
  
  @Nonnull
  public Single<Boolean> isValidSerialisation(@Nonnull final String serialisedShare);
}