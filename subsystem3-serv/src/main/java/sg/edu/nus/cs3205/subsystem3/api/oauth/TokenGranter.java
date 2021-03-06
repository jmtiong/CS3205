package sg.edu.nus.cs3205.subsystem3.api.oauth;

import java.security.InvalidKeyException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.pojos.AuthChallenge;
import sg.edu.nus.cs3205.subsystem3.pojos.ErrorResponse;
import sg.edu.nus.cs3205.subsystem3.pojos.GrantRequest;
import sg.edu.nus.cs3205.subsystem3.pojos.GrantRequest.GrantType;
import sg.edu.nus.cs3205.subsystem3.pojos.PasswordGrant;
import sg.edu.nus.cs3205.subsystem3.util.HttpHeaders;
import sg.edu.nus.cs3205.subsystem3.util.ResourceServerConnector;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TokenGranter {

    static final class SupportedGrantTypes {
        public GrantType[] supportedGrantTypes = GrantRequest.GrantType.values();
    }

    @GET
    public Response get() {
        return Response.ok(new SupportedGrantTypes()).build();
    }

    @POST
    public Response grant(final GrantRequest request,
            @HeaderParam(HttpHeaders.X_NFC_RESPONSE) final String nfcResponse,
            @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader)
            throws InvalidKeyException, JsonProcessingException {
        if (request == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing request body");
        } else if (request.grantType == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing/invalid grant_type field");
        } else if (request.username == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing username field");
        }

        if (request.grantType == GrantType.PASSWORD) {
            // Get challenge
            if (authorizationHeader == null) {
                // Get challenge and salt from server 4
                final Future<String> challenge = ResourceServerConnector.getChallengeAsync(request.username);
                final Future<String> salt = ResourceServerConnector.getUserSaltAsync(request.username);
                final Future<String> nfcChallenge = ResourceServerConnector
                        .getNFCChallengeAsync(request.username);
                try {
                    AuthChallenge authChallenge = new AuthChallenge(challenge.get(), salt.get());
                    return Response.status(Response.Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE,
                            new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                                    .writeValueAsString(authChallenge))
                            .header(HttpHeaders.X_NFC_CHALLENGE, nfcChallenge.get())
                            .entity(new ErrorResponse(Response.Status.UNAUTHORIZED.getReasonPhrase()))
                            .build();
                } catch (InterruptedException | ExecutionException e) {
                    throw new WebException(e);
                }
            } else {
                // User attempts to log in
                String[] authHeader = authorizationHeader.split(" ");
                if (authHeader.length < 2) {
                    throw new WebException(Response.Status.BAD_REQUEST, "Invalid Authorization Header.");
                }
                if (nfcResponse == null) {
                    throw new WebException(Response.Status.BAD_REQUEST, "Missing X-NFC-Response header");
                }

                request.userId = ResourceServerConnector.verifyResponse(request.username, authorizationHeader,
                        nfcResponse);
                try {
                    final String jwt = TokenUtils.createJWT(request);
                    return Response.ok(new PasswordGrant(jwt)).header(HttpHeaders.SET_AUTHORIZATION, jwt)
                            .build();
                } catch (JsonProcessingException | InvalidKeyException e) {
                    throw new WebException(e);
                }
            }
        }
        throw new WebException(Response.Status.BAD_REQUEST, "Unsupported %s grant_type", request.grantType);
    }
}
