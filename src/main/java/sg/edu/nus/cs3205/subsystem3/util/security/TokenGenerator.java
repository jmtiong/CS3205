package sg.edu.nus.cs3205.subsystem3.util.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class TokenGenerator {
    private static MessageDigest sha256;
    private static Mac hmacSHA256;
    static {
        try {
            sha256 = MessageDigest.getInstance(TokenConfigs.getConfig("TokenGenerator.algorithm.hash"));
            hmacSHA256 = Mac.getInstance(TokenConfigs.getConfig("TokenGenerator.algorithm.hmac"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String timeHash(String input, long epoch) {
        return hash(input + '.' + epoch);
    }

    public static String hash(String input) {
        byte[] hash = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String createJWT(String payloadJson) throws InvalidKeyException {
        String header = Base64.getUrlEncoder()
                .encodeToString(TokenConfigs.getConfig("TokenGenerator.jwt.header").getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String content = String.format("%s.%s", header, payload);

        hmacSHA256.init(
                new SecretKeySpec(TokenConfigs.getConfig("TokenGenerator.jwt.secret").getBytes(StandardCharsets.UTF_8),
                        TokenConfigs.getConfig("TokenGenerator.algorithm.hmac")));
        byte[] signatureBytes = hmacSHA256.doFinal(content.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder().encodeToString((signatureBytes));

        return String.format("%s.%s", content, signature);
    }
}
