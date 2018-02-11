package com.matthewtamlin.shamir.app.secretsharing.encoding;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ConstantConditions")
public class TestSecretEncoder {
  private SecretEncoder secretEncoder;
  
  @Before
  public void setup() {
    secretEncoder = new SecretEncoder();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testDecodeSecret_nullEncodedSecret() {
    secretEncoder.decodeSecret(null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testEncodeSecret_nullSecret() {
    secretEncoder.encodeSecret(null);
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsEmpty() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMinimumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{Byte.MIN_VALUE});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsNegative1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{-1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIs0() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsPositive1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMaximumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{Byte.MAX_VALUE});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndStartsWithMinimumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{Byte.MIN_VALUE, 3, 4});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndStartsWithNegative1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{-1, 3, 4});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndStartsWith0() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0, 3, 4});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndStartsWithPositive1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{1, 3, 4});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndStartsWithMaximumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{Byte.MAX_VALUE, 3, 4});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndEndsWithMinimumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{3, 4, Byte.MIN_VALUE});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndEndsWithNegative1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{3, 4, -1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndEndsWith0() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{3, 4, 0});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndEndsWithPositive1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{3, 4, 1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndEndsWithMaximumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{3, 4, Byte.MAX_VALUE});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndMiddleByteIsMinimumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0, Byte.MIN_VALUE, 1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndMiddleByteIsNegative1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0, -1, 1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndMiddleByteIs0() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0, 0, 1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndMiddleByteIsPositive1() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0, 1, 1});
  }
  
  @Test
  public void testEncodeAndDecodeSecret_secretIsMultipleBytesAndMiddleByteIsMaximumByte() {
    doEncodeAndDecodeSecretTestWithSecret(new byte[]{0, Byte.MAX_VALUE, 1});
  }
  
  private void doEncodeAndDecodeSecretTestWithSecret(final byte[] secret) {
    secretEncoder
        .encodeSecret(secret)
        .flatMap(secretEncoder::decodeSecret)
        .test()
        .awaitDone(200, MILLISECONDS)
        .assertNoErrors()
        .assertValue(reconstructedSecret -> Arrays.equals(secret, reconstructedSecret));
  }
}