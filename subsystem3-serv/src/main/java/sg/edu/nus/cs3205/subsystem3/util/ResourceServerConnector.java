package sg.edu.nus.cs3205.subsystem3.util;

import java.io.InputStream;
import java.security.InvalidKeyException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

public class ResourceServerConnector {
    static final String RESOURCE_SERVER_SESSION_PATH = "http://cs3205-4-i.comp.nus.edu.sg/api/team3";

    public static String getChallenge(final String username) {
        final String target = String.format("%s/%s", RESOURCE_SERVER_SESSION_PATH, "user/challenge");
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .queryParam("username", username).request();
        System.out.println("GET " + target);
        final Response response = client.get();
        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? response.readEntity(String.class) : TokenUtils.getFakeChallenge();
    }

    public static String getNFCChallenge(final String username) {
        final String target = String.format("%s/%s", RESOURCE_SERVER_SESSION_PATH, "user/nfcchallenge");
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .queryParam("username", username).request();
        System.out.println("GET " + target);
        final Response response = client.get();
        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? response.readEntity(String.class) : TokenUtils.getFakeChallenge();
    }

    public static String getUserSalt(final String username) throws InvalidKeyException {
        final String target;
        target = String.format("%s/%s", RESOURCE_SERVER_SESSION_PATH, "user", username);
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .queryParam("username", username).request();
        System.out.println("GET " + target);
        final Response response = client.get();
        return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                ? response.readEntity(String.class) : TokenUtils.getFakeSalt(username);
    }

    public static int verifyResponse(final String username, final String authorization,
            final String nfcResponse) {
        System.out.println(username + " " + authorization + " " + nfcResponse);
        final String target = String.format("%s/%s", RESOURCE_SERVER_SESSION_PATH, "user/login");
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .queryParam("username", username).request();
        System.out.println("POST " + target);
        final Response response = client
                .header(HttpHeaders.AUTHORIZATION, ResourseServerConfigs.getBasicAuthorization())
                .header(HttpHeaders.X_PASSWORD_RESPONSE, authorization)
                .header(HttpHeaders.X_NFC_RESPONSE, nfcResponse).post(null);
        if (response.getStatusInfo() == Response.Status.OK) {
            return Integer.parseInt(response.readEntity(String.class));
        } else {
            throw new WebException(Response.Status.UNAUTHORIZED, response.readEntity(String.class));
        }
    }

    public static int verifyNFCResponse(final String username, final String nfcResponse) {
        final String target = String.format("%s/%s", RESOURCE_SERVER_SESSION_PATH, "user/validatenfc");
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .queryParam("username", username).request();
        System.out.println("POST " + target);
        final Response response = client
                .header(HttpHeaders.AUTHORIZATION, ResourseServerConfigs.getBasicAuthorization())
                .header(HttpHeaders.X_NFC_RESPONSE, nfcResponse).post(null);
        if (response.getStatusInfo() == Response.Status.OK) {
            return Integer.parseInt(response.readEntity(String.class));
        } else {
            throw new WebException(Response.Status.UNAUTHORIZED, response.readEntity(String.class));
        }
    }

    public static Response getSession(final Integer userId, final Object type) {
        final String target = String.format("%s/%s/%d/all", RESOURCE_SERVER_SESSION_PATH, type, userId);
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .request(MediaType.APPLICATION_JSON_TYPE);
        System.out.println(HttpMethod.GET + ' ' + target);
        return client.get();
    }

    public static Response postSession(final Integer userId, final Object type, final long timestamp,
            final InputStream requestStream) {
        final String target = String.format("%s/%s/%d/upload/%d", RESOURCE_SERVER_SESSION_PATH, type, userId,
                timestamp);
        final Invocation.Builder client = ClientBuilder.newClient().target(target).request();
        System.out.println(HttpMethod.POST + ' ' + target);
        return client
                .post(Entity.entity(requestStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
    }

}
