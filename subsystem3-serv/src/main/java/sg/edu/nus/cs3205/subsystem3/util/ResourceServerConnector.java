package sg.edu.nus.cs3205.subsystem3.util;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    static final String RESOURCE_SERVER_HOST = "http://cs3205-4-i.comp.nus.edu.sg/api/team3/";

    private static Logger LOGGER = Logger.getLogger(ResourceServerConnector.class.getName());

    public static String getChallenge(String username) {
        final WebTarget webTarget = webTarget("user/challenge").queryParam("username", username);
        final Response response = ResourceServerConnector.request(HttpMethod.GET, webTarget);
        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? response.readEntity(String.class) : TokenUtils.getFakeChallenge();
    }

    public static String getNFCChallenge(final String username) {
        final WebTarget webTarget = webTarget("user/nfcchallenge").queryParam("username", username);
        final Response response = request(HttpMethod.GET, webTarget);
        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? response.readEntity(String.class) : TokenUtils.getFakeChallenge();
    }

    public static String getUserSalt(final String username) throws InvalidKeyException {
        final WebTarget webTarget = webTarget("user").queryParam("username", username);
        final Response response = request(HttpMethod.GET, webTarget);
        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? response.readEntity(String.class) : TokenUtils.getFakeSalt(username);
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
        return ClientBuilder.newClient().target(RESOURCE_SERVER_HOST + String.format(pathFormat, args));
    }

    private static Response request(final String method, final WebTarget webTarget,
            final String... headerPairs) {
        return request(method, webTarget, null, headerPairs);
    }

    private static Response request(final String method, final WebTarget webTarget, final Entity<?> entity,
            final String... headerPairs) {
        try {
            final Response response = requestAsync(method, webTarget, entity, headerPairs).get();
            LOGGER.info(response.getHeaders().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("; ")));
            return response;
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

}
