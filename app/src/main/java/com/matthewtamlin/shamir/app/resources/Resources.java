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