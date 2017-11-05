package sg.edu.nus.cs3205.subsystem3.api.session;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sg.edu.nus.cs3205.subsystem3.pojos.ErrorResponse;
import sg.edu.nus.cs3205.subsystem3.pojos.GrantClaim;
import sg.edu.nus.cs3205.subsystem3.util.HttpHeaders;
import sg.edu.nus.cs3205.subsystem3.util.ResourceServerConnector;

@Produces(MediaType.APPLICATION_JSON)
public final class UnauthorizedSession implements ISession {
    private Response response;

    public UnauthorizedSession(GrantClaim claim) {
        response = Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.X_NFC_CHALLENGE, ResourceServerConnector.getNFCChallenge(claim.username))
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
