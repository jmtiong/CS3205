package sg.edu.nus.cs3205.subsystem3.util.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.CredentialException;

import sg.edu.nus.cs3205.subsystem3.objects.GrantRequest;

public final class TokenUtils {
    private static final String hashAlgorithm = TokenConfigs.getConfig("algorithm.hash");
    private static final String hmacAlgorithm = TokenConfigs.getConfig("algorithm.hmac");
    private static final byte[] jwtHeaderBytes = TokenConfigs.getConfig("jwt.header", byte[].class);
    private static final byte[] secretBytes = TokenConfigs.getConfig("jwt.secret", byte[].class);
    private static final long jwtExpiration = TokenConfigs.getConfig("jwt.expiration", Long.class);
    private static final long jwtLeeway = TokenConfigs.getConfig("jwt.leeway", Long.class);

    private static final MessageDigest sha256;
    private static final Mac hmacSHA256;
    private static final Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Decoder BASE64_DECODER = Base64.getUrlDecoder();
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

    public static String createJWT(GrantRequest request) throws InvalidKeyException {
        return createJWT(request.getPasswordClaim(System.currentTimeMillis() + jwtExpiration));
    }

    public static String createJWT(String payloadJson) throws InvalidKeyException {
        String header = BASE64_ENCODER.encodeToString(jwtHeaderBytes);
        String payload = BASE64_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String content = String.format("%s.%s", header, payload);

        String signature = BASE64_ENCODER.encodeToString(createSignature(content));

        return String.format("%s.%s", content, signature);
    }

    public static void verifyJWT(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new GeneralSecurityException("Malformed JWT format");
        }

        byte[] signature = BASE64_DECODER.decode(parts[2]);
        if (!MessageDigest.isEqual(createSignature(String.format("%s.%s", parts[0], parts[1])), signature)) {
            throw new SignatureException("Invalid signature");
        }
        // verify claims
        String payload = new String(BASE64_DECODER.decode(parts[1]));
        Matcher matcher = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)").matcher(payload);
        if (matcher.find() && Long.parseLong(matcher.group(1)) < System.currentTimeMillis() - jwtLeeway) {
            throw new CredentialException("Token expired");
        }
    }

    public static String decodeString(String token){
      return new String(BASE64_DECODER.decode(token));
    }

    private static byte[] createSignature(String content) throws InvalidKeyException {
        hmacSHA256.init(new SecretKeySpec(secretBytes, hmacAlgorithm));
        return hmacSHA256.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }
}
