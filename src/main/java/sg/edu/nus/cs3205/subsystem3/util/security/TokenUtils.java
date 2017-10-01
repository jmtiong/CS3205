package sg.edu.nus.cs3205.subsystem3.util.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.CredentialException;

public final class TokenUtils {
    private static final String hashAlgorithm = TokenConfigs.getConfig("algorithm.hash");
    private static final String hmacAlgorithm = TokenConfigs.getConfig("algorithm.hmac");
    private static final byte[] jwtHeaderBytes = TokenConfigs.getConfig("jwt.header", byte[].class);
    private static final byte[] secretBytes = TokenConfigs.getConfig("jwt.secret", byte[].class);
    private static final long jwtLeeway = TokenConfigs.getConfig("jwt.leeway", long.class);

    private static MessageDigest sha256;
    private static Mac hmacSHA256;
    static {
        try {
            sha256 = MessageDigest.getInstance(hashAlgorithm);
            hmacSHA256 = Mac.getInstance(hmacAlgorithm);
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
        String header = Base64.getUrlEncoder().encodeToString(jwtHeaderBytes);
        String payload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String content = String.format("%s.%s", header, payload);

        String signature = Base64.getUrlEncoder().encodeToString(createSignature(content));

        return String.format("%s.%s", content, signature);
    }

    public static boolean verifyJWT(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new GeneralSecurityException("Malformed JWT format");
        }

        String header = new String(Base64.getUrlDecoder().decode(parts[0])),
                payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        byte[] signature = Base64.getUrlDecoder().decode(parts[2]);
        if (!MessageDigest.isEqual(createSignature(String.format("%s.%s", header, payload)), signature)) {
            throw new SignatureException("Invalid signature");
        }
        // verify claims
        Matcher matcher = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)").matcher(payload);
        if (matcher.find() && Long.parseLong(matcher.group(1)) < System.currentTimeMillis() - jwtLeeway) {
            throw new CredentialException("Token expired");
        }
        return true;
    }

    private static byte[] createSignature(String content) throws InvalidKeyException {
        hmacSHA256.init(new SecretKeySpec(secretBytes, hmacAlgorithm));
        byte[] signatureBytes = hmacSHA256.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return signatureBytes;
    }
}
