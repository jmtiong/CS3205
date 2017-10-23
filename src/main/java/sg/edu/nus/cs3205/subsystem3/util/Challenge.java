package sg.edu.nus.cs3205.subsystem3.util;

import java.util.Random;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class Challenge{

  public static byte[] generateChallenge(){
    byte[] challenge = new byte[32];
    new Random().nextBytes(challenge);
    return challenge;
  }

  public static byte[] generateExpectedResponse(byte[] challenge){
    // create from array
    BigInteger bigInt = new BigInteger(challenge);

    // shift
    BigInteger shiftInt = bigInt.shiftRight(4);

    // back to array
    byte [] shifted = shiftInt.toByteArray();
    return shifted;
  }

  public static boolean validateResponse(byte[] response, byte[] challenge, byte[] passwordHash){
    try{
      // h(h(h(pwd)) + c) + h(pwd) = response
      //XOR hash of password hash with challenge
      byte[] expectedResult = computeXOR(generateHash(passwordHash), challenge); // h(h(pwd)) + c
      //hash the result
      expectedResult = generateHash(expectedResult); // h(h(h(pwd)) + c)
      //XOR result with challenge response
      expectedResult = computeXOR(expectedResult, response); // h(h(h(pwd)) + c) + h(h(h(pwd)) + c) + h(pwd) = h(pwd)
      //hash the result
      expectedResult = generateHash(expectedResult); // h(h(pwd))

      return Arrays.equals(expectedResult, generateHash(passwordHash));
    } catch(Exception e){
      e.printStackTrace();
    }
    return false;
  }

  private static byte[] computeXOR(byte[] b1, byte[] b2) {
    byte[] result = new byte[32];
    for (int i = 0; i < 32; i++) {
        result[i] = (byte)(b1[i] ^ b2[i]);
    }
    return result;
  }

  public static byte[] generateHash(byte[] input) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return digest.digest(input);
}
}
