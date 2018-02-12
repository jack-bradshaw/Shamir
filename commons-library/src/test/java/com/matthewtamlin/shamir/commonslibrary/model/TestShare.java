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

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for the {@link Share} class.
 */
public class TestShare {
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_indexNeverSet() {
    Share
        .builder()
        .setValue(0)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_valueNeverSet() {
    Share
        .builder()
        .setIndex(1)
        .build();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testInstantiation_indexLessThan1() {
    Share.builder()
        .setIndex(0)
        .setValue(0)
        .build();
  }
  
  @Test
  public void testInstantiation_indexEqualTo1() {
    Share
        .builder()
        .setIndex(1)
        .setValue(0)
        .build();
  }
  
  @Test
  public void testInstantiation_indexGreaterThan1() {
    Share
        .builder()
        .setIndex(2)
        .setValue(0)
        .build();
  }
  
  @Test(expected = NullPointerException.class)
  public void testInstantiation_nullIndex() {
    Share
        .builder()
        .setIndex(null)
        .setValue(0)
        .build();
  }
  
  @Test
  public void testInstantiation_negativeValue() {
    Share
        .builder()
        .setIndex(1)
        .setValue(-1)
        .build();
  }
  
  @Test
  public void testInstantiation_zeroValue() {
    Share
        .builder()
        .setIndex(1)
        .setValue(0)
        .build();
  }
  
  @Test
  public void testInstantiation_positiveValue() {
    Share
        .builder()
        .setIndex(1)
        .setValue(1)
        .build();
  }
  
  @Test(expected = NullPointerException.class)
  public void testInstantiation_nullValue() {
    Share
        .builder()
        .setIndex(1)
        .setValue(null)
        .build();
  }
  
  @Test
  public void testInstantiateThenGet_builtUsingBigIntegers() {
    final Share s = Share
        .builder()
        .setIndex(BigInteger.ONE)
        .setValue(BigInteger.TEN)
        .build();
    
    assertThat(s.getIndex(), is(BigInteger.ONE));
    assertThat(s.getValue(), is(BigInteger.TEN));
  }
  
  @Test
  public void testInstantiateThenGet_builtUsingIntegers() {
    final Share s = Share
        .builder()
        .setIndex(1)
        .setValue(10)
        .build();
    
    assertThat(s.getIndex(), is(BigInteger.ONE));
    assertThat(s.getValue(), is(BigInteger.TEN));
  }
}