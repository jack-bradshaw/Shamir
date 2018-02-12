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

package com.matthewtamlin.shamir.reactivejavaapi.crypto;

import com.google.common.collect.ImmutableSet;
import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static java.math.BigInteger.ONE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Unit tests for the {@link RxShamir} class.
 */
@SuppressWarnings("ConstantConditions")
public class TestRxShamir {
  private static final BigInteger FIVE = BigInteger.valueOf(5);
  
  private static final BigInteger SEVEN = BigInteger.valueOf(7);
  
  private RxShamir rxShamir;
  
  @Before
  public void setup() {
    rxShamir = new RxShamir(new SecureRandom());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRandom() {
    new RxShamir(null);
  }
  
  @Test
  public void testInstantiate_nonNullRandom() {
    new RxShamir(new SecureRandom());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiateStatically_nullRandom() {
    RxShamir.create(null);
  }
  
  @Test
  public void testInstantiateStatically_nonNullRandom() {
    RxShamir.create(new SecureRandom());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateShares_nullSecret() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir.createShares(null, creationScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateShares_nullCreationScheme() {
    rxShamir.createShares(ONE, null);
  }
  
  @Test
  public void testCreateShares_secretLessThanPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
    
    rxShamir
        .createShares(FIVE, creationScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test
  public void testCreateShares_secretEqualToPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(5)
        .build();
    
    rxShamir
        .createShares(FIVE, creationScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testCreateShares_secretGreaterThanPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(5)
        .build();
    
    rxShamir
        .createShares(SEVEN, creationScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRecoverSecret_nullShares() {
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir.recoverSecret(null, recoveryScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRecoverSecret_sharesContainsNull() {
    final Set<Share> shares = new HashSet<>();
    
    shares.add(Share.builder().setIndex(1).setValue(1).build());
    shares.add(null);
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir.recoverSecret(shares, recoveryScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRecoverSecret_nullRecoveryScheme() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(1).build())
        .add(Share.builder().setIndex(1).setValue(2).build())
        .build();
    
    rxShamir.recoverSecret(shares, null);
  }
  
  @Test
  public void testRecoverSecret_duplicateShareIndex() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(1).build())
        .add(Share.builder().setIndex(1).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testRecoverSecret_shareIndexLessThanPrime() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(1).build())
        .add(Share.builder().setIndex(2).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test
  public void testRecoverSecret_shareIndexEqualToPrime() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(7).setValue(1).build())
        .add(Share.builder().setIndex(2).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testRecoverSecret_shareIndexGreaterThanPrime() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(8).setValue(1).build())
        .add(Share.builder().setIndex(2).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testRecoverSecret_shareValueLessThanPrime() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(1).build())
        .add(Share.builder().setIndex(2).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertComplete();
  }
  
  @Test
  public void testRecoverSecret_shareValueEqualToPrime() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(7).build())
        .add(Share.builder().setIndex(2).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testRecoverSecret_shareValueGreaterThanPrime() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(8).build())
        .add(Share.builder().setIndex(2).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    rxShamir
        .recoverSecret(shares, recoveryScheme)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertError(IllegalStateException.class)
        .assertNotComplete();
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_twoTotalParts_noSharesRecovered() {
    createSharesAndRecoverSecret(2, 2, 0, false);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_twoTotalParts_oneShareRecovered() {
    createSharesAndRecoverSecret(2, 2, 1, false);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_twoTotalParts_twoSharesRecovered() {
    createSharesAndRecoverSecret(2, 2, 2, true);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_noShareRecovered() {
    createSharesAndRecoverSecret(2, 3, 0, false);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_oneShareRecovered() {
    createSharesAndRecoverSecret(2, 3, 1, false);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_twoShareRecovered() {
    createSharesAndRecoverSecret(2, 3, 2, true);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_threeSharesRecovered() {
    createSharesAndRecoverSecret(2, 3, 3, true);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_tenRequiredParts_oneHundredTotalParts_nineSharesRecovered() {
    createSharesAndRecoverSecret(10, 100, 9, false);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_tenRequiredParts_oneHundredTotalParts_tenShareRecovered() {
    createSharesAndRecoverSecret(10, 100, 10, true);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_tenRequiredParts_oneHundredTotalParts_elevenSharesRecovered() {
    createSharesAndRecoverSecret(10, 100, 11, true);
  }
  
  private void createSharesAndRecoverSecret(
      final int requiredShareCount,
      final int totalShareCount,
      final int recoveredShareCount,
      final boolean expectPass) {
    
    final BigInteger secret = new BigInteger("1298074214633706835075030044377087");
    final BigInteger prime = new BigInteger("1298074214633706835075030044421213");
    
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(requiredShareCount)
        .setTotalShareCount(totalShareCount)
        .setPrime(prime)
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(requiredShareCount)
        .setPrime(prime)
        .build();
    
    final RxShamir shamir = new RxShamir(new SecureRandom());
    
    final Observable<Share> allShares = shamir.createShares(secret, creationScheme);
    
    final Observable<Share> recoveredShares = allShares
        .take(recoveredShareCount);
    
    final Single<BigInteger> recoverSecret = recoveredShares
        .collectInto(new HashSet<Share>(), Set::add)
        .flatMap(recoveredShareSet -> shamir.recoverSecret(recoveredShareSet, recoveryScheme));
    
    if (expectPass) {
      recoverSecret
          .test()
          .assertNoErrors()
          .assertComplete()
          .assertValue(secret);
      
    } else {
      recoverSecret
          .test()
          .assertError(IllegalStateException.class)
          .assertNotComplete();
    }
  }
}