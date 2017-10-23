package sg.edu.nus.cs3205.subsystem3.nfcapplet;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;

public class NFCSecretGenerator {
    public static String generateSecret() throws NoSuchAlgorithmException {
        final KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        return Base64.getUrlEncoder().encodeToString(keyGen.generateKey().getEncoded());
    }
}
