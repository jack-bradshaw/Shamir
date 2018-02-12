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

package com.matthewtamlin.shamir.commonslibrary.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the {@link Pair} class.
 */
public class TestPair {
  @Test(expected = NullPointerException.class)
  public void testInstantiate_nullKey() {
    Pair.create(null, 123456);
  }
  
  @Test(expected = NullPointerException.class)
  public void testInstantiate_nullValue() {
    Pair.create("test", null);
  }
  
  @Test
  public void testInstantiateAndGet() {
    final Pair<String, Integer> pair = Pair.create("test", 123456);
    
    assertThat(pair.getKey(), is("test"));
    assertThat(pair.getValue(), is(123456));
  }
}