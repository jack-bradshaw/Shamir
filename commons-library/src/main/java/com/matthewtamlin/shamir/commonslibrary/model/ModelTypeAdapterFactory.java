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

package com.matthewtamlin.shamir.commonslibrary.model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nonnull;

/**
 * Gson TypeAdapterFactory for all classes in the {@link com.matthewtamlin.shamir.commonslibrary.model} package.
 */
public class ModelTypeAdapterFactory implements TypeAdapterFactory {
  @Override
  @SuppressWarnings("unchecked")
  public <T> TypeAdapter<T> create(@Nonnull final Gson gson, @Nonnull final TypeToken<T> type) {
    final Class<T> rawType = (Class<T>) type.getRawType();
    
    if (CreationScheme.class.isAssignableFrom(rawType)) {
      return (TypeAdapter<T>) CreationScheme.typeAdapter(gson);
      
    } else if (RecoveryScheme.class.isAssignableFrom(rawType)) {
      return (TypeAdapter<T>) RecoveryScheme.typeAdapter(gson);
      
    } else if (Share.class.isAssignableFrom(rawType)) {
      return (TypeAdapter<T>) Share.typeAdapter(gson);
      
    } else {
      return null;
    }
  }
}