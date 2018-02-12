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

package com.matthewtamlin.shamir.standardjavaapi.crypto;

import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

/**
 * Performs the share creation and secret recovery operations of Shamir's Secret Sharing. All cryptographic operations
 * are performed in a finite field to prevent geometric attacks.
 * <p>
 * For convenience, this class can be instantiated using either the {@link #Shamir(SecureRandom)} constructor or the
 * {@link #create(SecureRandom)} static method.
 */
public class Shamir {
  private final RxShamir rxShamir;
  
  /**
   * Constructs a new Shamir.
   * <p>
   * The {@link #create(SecureRandom)} static method is provided as an alternative to this constructor.
   *
   * @param random
   *     the random source to use in the cryptographic operations, not null
   */
  public Shamir(@Nonnull final SecureRandom random) {
    checkNotNull(random, "\'random\' must not be null.");
    
    rxShamir = RxShamir.create(random);
  }
  
  /**
   * Constructs a new Shamir.
   * <p>
   * The {@link #Shamir(SecureRandom)} constructor is provided as an alternative to this method.
   *
   * @param random
   *     the random source to use in the cryptographic operations, not null
   */
  @Nonnull
  public static Shamir create(@Nonnull final SecureRandom random) {
    return new Shamir(random);
  }
  
  /**
   * Splits a secret into a set of shares using Shamir's Secret Sharing.
   *
   * @param secret
   *     the secret to share, not null
   * @param creationScheme
   *     defines the sharing configuration, not null
   *
   * @return the shares, not null, not containing null
   *
   * @throws IllegalStateException
   *     if the secret is not less than the prime specified in the creation scheme
   */
  @Nonnull
  public Set<Share> createShares(@Nonnull final BigInteger secret, @Nonnull final CreationScheme creationScheme) {
    return rxShamir
        .createShares(secret, creationScheme)
        .collectInto(new HashSet<Share>(), Set::add)
        .blockingGet();
  }
  
  /**
   * Recovers a secret from a set of shares using Shamir's Secret Sharing.
   *
   * @param shares
   *     the shares to reconstruct the secret from, not null, not containing null
   * @param recoveryScheme
   *     defines the recovery configuration, not null
   *
   * @return the recovered secret, not null
   *
   * @throws IllegalStateException
   *     if two or more shares have the same index
   * @throws IllegalStateException
   *     if the number of shares is less than the required share count specified in the recovery scheme
   * @throws IllegalStateException
   *     if the index of any share is greater than or equal to the prime specified in the recovery scheme
   * @throws IllegalStateException
   *     if the value of any share is greater than or equal to the prime specified in the recovery scheme
   */
  @Nonnull
  public BigInteger recoverSecret(@Nonnull final Set<Share> shares, @Nonnull final RecoveryScheme recoveryScheme) {
    return rxShamir.recoverSecret(shares, recoveryScheme).blockingGet();
  }
}