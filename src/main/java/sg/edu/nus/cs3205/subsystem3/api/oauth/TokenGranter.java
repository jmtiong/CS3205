package sg.edu.nus.cs3205.subsystem3.api.oauth;

import java.security.InvalidKeyException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.objects.GrantRequest;
import sg.edu.nus.cs3205.subsystem3.objects.GrantRequest.GrantType;
import sg.edu.nus.cs3205.subsystem3.objects.PasswordGrant;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TokenGranter {
    @GET
    public Response get() {
        return Response.ok(new Object() {
            @SuppressWarnings("unused")
            public GrantType[] supportedGrantTypes = GrantRequest.GrantType.values();
        }).build();
    }

    @POST
    public Response grant(final GrantRequest request, @HeaderParam("X-NFC-Token") final String nfcToken) {
        if (nfcToken == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing X-NFC-Token header");
        }
        if (request == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing request body");
        } else if (request.grantType == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing/invalid grant_type");
        } else if (request.grantType == GrantType.PASSWORD) {
            if (request.username != null && request.passhash != null) {
                try {
                    request.userId = 1;
                    final String jwt = TokenUtils.createJWT(request);
                    return Response.ok(new PasswordGrant(jwt)).build();
                } catch (JsonProcessingException | InvalidKeyException e) {
                    throw new WebException(e);
                }
            }
            throw new WebException(Response.Status.UNAUTHORIZED, "Invalid credential");
        }
        throw new WebException(Response.Status.BAD_REQUEST, "Unknown %s grant_type", request.grantType);
    }
}
