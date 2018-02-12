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

package com.matthewtamlin.shamir.app.resources;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.reactivex.Single;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.String.format;

public class Resources {
  final JsonObject strings;
  
  public Resources() {
    try (final InputStreamReader in = new InputStreamReader(getClass().getResourceAsStream("/strings/en.json"))) {
      strings = new JsonParser()
          .parse(in)
          .getAsJsonObject();
      
    } catch (final IOException e) {
      throw new RuntimeException("Unable to load string resources from /strings/en.json");
    }
  }
  
  @Nonnull
  public Single<String> getString(@Nonnull final String key) {
    return Single.fromCallable(() -> {
      if (strings.has(key)) {
        return strings.get(key).getAsString();
        
      } else {
        throw new RuntimeException(format("No string resource exists for key \'%1$s\'", key));
      }
    });
  }
  
  @Nonnull
  public String blockingGetString(@Nonnull final String key) {
    return getString(key).blockingGet();
  }
  
  @Nonnull
  public Single<Node> getLayout(@Nonnull final String location) {
    return Single.create(emitter -> {
      try {
        emitter.onSuccess(FXMLLoader.load(getClass().getResource("/layouts/" + location)));
        
      } catch (final IOException e) {
        emitter.onError(new RuntimeException("Unable to load resource /layouts/" + location, e));
      }
    });
  }
  
  @Nonnull
  public Node blockingGetLayout(@Nonnull final String location) {
    return getLayout(location).blockingGet();
  }
}