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
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ConstantConditions")
public class TestJsonShareSerialiser {
  private JsonShareSerialiser serialiser;
  
  @Before
  public void setup() {
    serialiser = new JsonShareSerialiser();
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