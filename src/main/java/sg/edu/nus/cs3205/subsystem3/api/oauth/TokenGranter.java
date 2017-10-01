package sg.edu.nus.cs3205.subsystem3.api.oauth;

import java.security.InvalidKeyException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response grant(GrantRequest request, @HeaderParam("X-NFC-Token") String nfcToken) {
        if (nfcToken == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing X-NFC-Token header");
        }
        if (request.getGrantType() == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing grant_type field");
        } else if (request.getGrantType() == GrantType.PASSWORD) {
            if (request.getUsername() != null && request.getPasshash() != null) {
                try {
                    String jwt = TokenUtils.createJWT(request);
                    return Response.ok(new PasswordGrant(jwt)).build();
                } catch (InvalidKeyException e) {
                    throw new WebException(e);
                }
            }
            throw new WebException(Response.Status.UNAUTHORIZED, "Invalid credential");
        }
        throw new WebException();
    }
}
