package com.matthewtamlin.shamir.app.secretsharing.serialisation;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matthewtamlin.shamir.commonslibrary.model.ModelTypeAdapterFactory;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ConstantConditions")
public class TestJsonShareSerialiser {
  private JsonShareSerialiser serialiser;
  
  @Before
  public void setup() {
    final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new ModelTypeAdapterFactory())
        .create();
    
    serialiser = new JsonShareSerialiser(gson);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSerialise_nullShare() {
    serialiser.serialise(null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDeserialise_nullSerialisedShare() {
    serialiser.deserialise(null);
  }
  
  @Test
  public void testSerialiseAndDeserialise() {
    final Share share = Share
        .builder()
        .setIndex(1)
        .setValue(2)
        .build();
    
    serialiser
        .serialise(share)
        .flatMap(serialiser::deserialise)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(share);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIsValidSerialisation_nullSerialisedShare() {
    serialiser.isValidSerialisation(null);
  }
  
  @Test
  public void testIsValidSerialisation_emptySerialisedShare() {
    serialiser
        .isValidSerialisation("")
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testIsValidSerialisation_malformedInput() {
    serialiser
        .isValidSerialisation("Hello, World!")
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(false);
  }
  
  @Test
  public void testIsValidSerialisation_wellFormedInput() {
    final Share share = Share
        .builder()
        .setIndex(1)
        .setValue(2)
        .build();
    
    serialiser
        .serialise(share)
        .flatMap(serialiser::isValidSerialisation)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
}