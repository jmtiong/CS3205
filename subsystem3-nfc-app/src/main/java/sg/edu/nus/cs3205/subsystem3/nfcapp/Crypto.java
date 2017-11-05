package sg.edu.nus.cs3205.subsystem3.nfcapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final String KEY_FILE_PATH = "crypto.conf";
    public static final int KEY_SIZE = 256;
    public static final int IV_SIZE = 16;
    public static final String AES_METHOD = "AES/CBC/PKCS5Padding";
    public static final String ENCRYPTION = "AES";
    private static SecretKeySpec key;

    static {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Crypto.class.getClassLoader().getResourceAsStream(KEY_FILE_PATH)))) {
            String encodedKey = reader.readLine();
            final byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            key = new SecretKeySpec(decodedKey, ENCRYPTION);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static String decrypt(final String encryptedData) throws GeneralSecurityException {
        System.out.println("enc: " + encryptedData);
        final byte[] iv = new byte[IV_SIZE];
        final byte[] t1 = Base64.getDecoder().decode(encryptedData);
        ByteBuffer buffer = ByteBuffer.wrap(t1);
        System.out.println(Arrays.toString(t1));
        buffer = buffer.get(iv).slice();
        System.out.println(Arrays.toString(iv));
        final byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        System.out.println(Arrays.toString(data));
        final Cipher cipher = Cipher.getInstance(AES_METHOD);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        final byte[] temp = cipher.doFinal(data);
        System.out.println(Arrays.toString(temp));
        System.out.println("dec: " + Base64.getEncoder().encodeToString(temp));
        return Base64.getEncoder().encodeToString(temp);
    }

}
