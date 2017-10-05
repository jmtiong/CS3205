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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.CredentialException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import sg.edu.nus.cs3205.subsystem3.objects.GrantClaim;
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
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    private static final Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Decoder BASE64_DECODER = Base64.getUrlDecoder();
    static {
        try {
            sha256 = MessageDigest.getInstance(hashAlgorithm);
            hmacSHA256 = Mac.getInstance(hmacAlgorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String timeHash(final String input, final long epoch) {
        return hash(input + '.' + epoch);
    }

    public static String hash(final String input) {
        final byte[] hash = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String createJWT(final GrantRequest request)
            throws JsonProcessingException, InvalidKeyException {
        return createJWT(request.getPasswordClaim(System.currentTimeMillis() + jwtExpiration));
    }

    public static String createJWT(final Object claim) throws JsonProcessingException, InvalidKeyException {
        final String header = BASE64_ENCODER.encodeToString(jwtHeaderBytes);
        final String payload = BASE64_ENCODER.encodeToString(MAPPER.writeValueAsBytes(claim));
        final String content = String.format("%s.%s", header, payload);

        final String signature = BASE64_ENCODER.encodeToString(createSignature(content));

        return String.format("%s.%s", content, signature);
    }

    public static GrantClaim verifyJWT(final String jwt) throws Exception {
        final String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new GeneralSecurityException("Malformed JWT format");
        }

        final byte[] signature = BASE64_DECODER.decode(parts[2]);
        if (!MessageDigest.isEqual(createSignature(String.format("%s.%s", parts[0], parts[1])), signature)) {
            throw new SignatureException("Invalid signature");
        }
        // verify claims
        final String payload = new String(BASE64_DECODER.decode(parts[1]));
        final GrantClaim claim = MAPPER.readValue(payload, GrantClaim.class);
        if (claim.exp != null && claim.exp < System.currentTimeMillis() - jwtLeeway) {
            throw new CredentialException("Token expired");
        }
        return claim;
    }

    private static byte[] createSignature(final String content) throws InvalidKeyException {
        hmacSHA256.init(new SecretKeySpec(secretBytes, hmacAlgorithm));
        return hmacSHA256.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }
}
