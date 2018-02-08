package com.matthewtamlin.shamir.app.secretsharing.serialisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matthewtamlin.shamir.commonslibrary.model.ModelTypeAdapterFactory;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ConstantConditions")
public class TestJsonRecoverySchemeSerialiser {
  private JsonRecoverySchemeSerialiser serialiser;
  
  @Before
  public void setup() {
    final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new ModelTypeAdapterFactory())
        .create();
    
    serialiser = new JsonRecoverySchemeSerialiser(gson);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSerialise_nullRecoveryScheme() {
    serialiser.serialise(null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDeserialise_nullSerialisedRecoveryScheme() {
    serialiser.deserialise(null);
  }
  
  @Test
  public void testSerialiseAndDeserialise() {
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    serialiser
        .serialise(recoveryScheme)
        .flatMap(serialiser::deserialise)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(recoveryScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIsValidSerialisation_nullSerialisedScheme() {
    serialiser.isValidSerialisation(null);
  }
  
  @Test
  public void testIsValidSerialisation_emptySerialisedScheme() {
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
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    serialiser
        .serialise(recoveryScheme)
        .flatMap(serialiser::isValidSerialisation)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(true);
  }
}