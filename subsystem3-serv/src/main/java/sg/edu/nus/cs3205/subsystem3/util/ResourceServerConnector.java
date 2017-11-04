package sg.edu.nus.cs3205.subsystem3.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

public class ResourceServerConnector {
    static final String RESOURCE_SERVER_HOST = "https://cs3205-4-i.comp.nus.edu.sg/api/team3/";

    private static Logger LOGGER = Logger.getLogger(ResourceServerConnector.class.getName());

    public static String getChallenge(final String username) {
        return resolve(getChallengeAsync(username));
    }

    public static Future<String> getChallengeAsync(final String username) {
        final WebTarget webTarget = webTarget("user/challenge").queryParam("username", username);
        final Future<Response> futureResponse = requestAsync(HttpMethod.GET, webTarget);
        return CompletableFuture.supplyAsync(() -> {
            final Response response = resolve(futureResponse);
            return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                    ? response.readEntity(String.class) : TokenUtils.getFakeChallenge();
        });
    }

    public static String getNFCChallenge(final String username) {
        return resolve(getNFCChallengeAsync(username));
    }

    public static Future<String> getNFCChallengeAsync(final String username) {
        final WebTarget webTarget = webTarget("user/nfcchallenge").queryParam("username", username);
        final Future<Response> futureResponse = requestAsync(HttpMethod.GET, webTarget);
        return CompletableFuture.supplyAsync(() -> {
            final Response response = resolve(futureResponse);
            return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                    ? response.readEntity(String.class) : TokenUtils.getFakeChallenge();
        });
    }

    public static String getUserSalt(final String username) throws InvalidKeyException {
        return resolve(getUserSaltAsync(username));
    }

    public static Future<String> getUserSaltAsync(final String username) {
        final WebTarget webTarget = webTarget("user").queryParam("username", username);
        final Future<Response> futureResponse = requestAsync(HttpMethod.GET, webTarget);
        return CompletableFuture.supplyAsync(() -> {
            final Response response = resolve(futureResponse);
            try {
                return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                        ? response.readEntity(String.class) : TokenUtils.getFakeSalt(username);
            } catch (final Exception e) {
                throw new WebException(e);
            }
        });
    }

    public static int verifyResponse(final String username, final String authorization,
            final String nfcResponse) {
        final WebTarget webTarget = webTarget("user/login").queryParam("username", username);
        final Response response = request(HttpMethod.POST, webTarget, HttpHeaders.X_PASSWORD_RESPONSE,
                authorization, HttpHeaders.X_NFC_RESPONSE, nfcResponse);
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            return Integer.parseInt(response.readEntity(String.class));
        } else {
            throw new WebException(response, Response.Status.UNAUTHORIZED);
        }
    }

    public static int verifyNFCResponse(final String username, final String nfcResponse) {
        final WebTarget webTarget = webTarget("user/validatenfc").queryParam("username", username);
        final Response response = request(HttpMethod.POST, webTarget, HttpHeaders.X_NFC_RESPONSE,
                nfcResponse);
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            return Integer.parseInt(response.readEntity(String.class));
        } else {
            throw new WebException(response, Response.Status.UNAUTHORIZED);
        }
    }

    public static Response getSession(final Integer userId, final Object type) {
        final WebTarget newWebTarget = webTarget("%s/%d/all", type, userId);
        return request(HttpMethod.GET, newWebTarget);
    }

    public static Response postSession(final Integer userId, final Object type, final long timestamp,
            final InputStream requestStream) {
        final WebTarget webTarget = webTarget("%s/%d/upload/%d", type, userId, timestamp);
        return request(HttpMethod.POST, webTarget,
                Entity.entity(requestStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
    }

    private static WebTarget webTarget(final String pathFormat, final Object... args) {
        try {
            return ClientBuilder.newBuilder().sslContext(getSSLContext()).build()
                    .target(RESOURCE_SERVER_HOST + String.format(pathFormat, args));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Can't get SSL context", e);
            throw new WebException(Response.Status.INTERNAL_SERVER_ERROR, "Can't get SSL context");
        }
    }

    private static Response request(final String method, final WebTarget webTarget,
            final String... headerPairs) {
        return request(method, webTarget, null, headerPairs);
    }

    private static Response request(final String method, final WebTarget webTarget, final Entity<?> entity,
            final String... headerPairs) {
        return resolve(requestAsync(method, webTarget, entity, headerPairs));
    }

    private static <T> T resolve(final Future<T> future) throws WebException {
        try {
            final T result = future.get();
            if (result instanceof Response) {
                LOGGER.info(((Response) result).getHeaders().entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("; ")));
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new WebException(e);
        }
    }

    private static Future<Response> requestAsync(final String method, final WebTarget webTarget,
            final String... headerPairs) {
        return requestAsync(method, webTarget, null, headerPairs);
    }

    private static Future<Response> requestAsync(final String method, final WebTarget webTarget,
            final Entity<?> entity, final String... headerPairs) {
        LOGGER.info(method + ' ' + webTarget.getUri());
        Invocation.Builder builder = webTarget.request().header(HttpHeaders.AUTHORIZATION,
                ResourseServerConfigs.getBasicAuthorization());
        for (int i = 1; i < headerPairs.length; i += 2) {
            builder = builder.header(headerPairs[i - 1], headerPairs[i]);
        }
        return builder.async().method(method, entity);
    }

    private static SSLContext getSSLContext() throws Exception {
        final SSLContext context = SSLContext.getInstance("TLS");
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        try (final InputStream is = new FileInputStream(ResourseServerConfigs.getKeyStore())) {
            keyStore.load(is, ResourseServerConfigs.getSSLPassword().toCharArray());
        }
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, ResourseServerConfigs.getSSLPassword().toCharArray());
        final KeyStore trustedStore = KeyStore.getInstance("JKS");
        try (final InputStream is = new FileInputStream(ResourseServerConfigs.getTrustStore())) {
            trustedStore.load(is, ResourseServerConfigs.getSSLPassword().toCharArray());
        }
        final TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustedStore);
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());
        return context;
    }
}
