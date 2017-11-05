package sg.edu.nus.cs3205.subsystem3;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import sg.edu.nus.cs3205.subsystem3.api.Authenticator;
import sg.edu.nus.cs3205.subsystem3.api.oauth.TokenGranter;
import sg.edu.nus.cs3205.subsystem3.api.session.ISession;
import sg.edu.nus.cs3205.subsystem3.pojos.Links;
import sg.edu.nus.cs3205.subsystem3.util.HttpHeaders;

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
        return Authenticator.authenticateTokenGranter();
    }

    @Path("/session")
    public ISession session(@HeaderParam(HttpHeaders.AUTHORIZATION) final String accessToken,
            @HeaderParam(HttpHeaders.X_NFC_RESPONSE) final String nfcResponse) {
        return Authenticator.authenticateSession(accessToken, nfcResponse);
    }
}
