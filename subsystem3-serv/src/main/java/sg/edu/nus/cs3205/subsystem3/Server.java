package sg.edu.nus.cs3205.subsystem3;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import sg.edu.nus.cs3205.subsystem3.api.oauth.TokenGranter;
import sg.edu.nus.cs3205.subsystem3.api.session.Session;
import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.pojos.ErrorResponse;
import sg.edu.nus.cs3205.subsystem3.pojos.GrantClaim;
import sg.edu.nus.cs3205.subsystem3.pojos.Links;
import sg.edu.nus.cs3205.subsystem3.util.HttpHeaders;
import sg.edu.nus.cs3205.subsystem3.util.ResourceServerConnector;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Server {
    @GET
    public Response getRoot(@Context final UriInfo uri) {
        return Response.ok(new Links(Links.newLink(uri, "", "self", HttpMethod.GET),
                Links.newLink(uri, "oauth/token", "oauth.token", HttpMethod.POST),
                Links.newLink(uri, "session", "session", HttpMethod.POST))).build();
    }

    @Path("/oauth/token")
    public TokenGranter grant() {
        return new TokenGranter();
    }

    @Path("/session")
    public Object session(@HeaderParam(HttpHeaders.AUTHORIZATION) final String accessToken,
            @HeaderParam(HttpHeaders.X_NFC_RESPONSE) final String nfcResponse) {
        GrantClaim claim;
        if (accessToken == null) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Missing Authorization header");
        } else if (!accessToken.startsWith("Bearer ")) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Bearer access token required");
        }
        try {
            claim = TokenUtils.verifyJWT(accessToken.substring("Bearer ".length()));
        } catch (final Exception e) {
            throw new WebException(Response.Status.UNAUTHORIZED, e);
        }

        if (nfcResponse == null) {
            return new UnauthorizedSession(claim);
        } else {
            try {
                ResourceServerConnector.verifyNFCResponse(claim.username, nfcResponse);
            } catch (final Exception e) {
                throw new WebException(Response.Status.UNAUTHORIZED, e);
            }

            return new Session(claim);
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    public static final class UnauthorizedSession {
        private Response response;

        public UnauthorizedSession(GrantClaim claim) {
            response = Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.X_NFC_CHALLENGE,
                            ResourceServerConnector.getNFCChallenge(claim.username))
                    .entity(new ErrorResponse(Response.Status.UNAUTHORIZED.getReasonPhrase())).build();
        }

        @GET
        @Path("{any: .*}")
        public Response get() {
            return response;
        }

        @POST
        @Path("{any: .*}")
        public Response post() {
            return response;
        }
    }
}
