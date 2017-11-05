package sg.edu.nus.cs3205.subsystem3.api.session;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.pojos.GrantClaim;
import sg.edu.nus.cs3205.subsystem3.pojos.Link;
import sg.edu.nus.cs3205.subsystem3.pojos.Links;
import sg.edu.nus.cs3205.subsystem3.util.HttpHeaders;
import sg.edu.nus.cs3205.subsystem3.util.ResourceServerConnector;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_JSON)
public class Session implements ISession {
    public static enum SessionType {
        HEART("heart"), STEP("step"), IMAGE("image"), VIDEO("video");

        public static SessionType fromString(final String value) {
            try {
                return Stream.of(SessionType.values()).filter(type -> value.startsWith(type.path)).findAny()
                        .get();
            } catch (NullPointerException | NoSuchElementException e) {
                throw new IllegalArgumentException(String.format("Invalid session type %s in path", value));
            }
        }

        public String path;

        private SessionType(final String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return this.path;
        }
    }

    private final GrantClaim claim;

    public Session(final GrantClaim claim) {
        this.claim = claim;
    }

    @GET
    public Response getRoot(@Context final UriInfo uri) {
        return wrapJWTRefresh(
                Response.ok(new Links(Stream
                        .concat(Stream.of(Links.newLink(uri, "", "self", HttpMethod.GET)),
                                Stream.of(SessionType.values())
                                        .map(type -> Links.newLink(uri, type.toString(), "session." + type,
                                                HttpMethod.GET + ',' + HttpMethod.POST)))
                        .toArray(Link[]::new))).build());
    }

    @GET
    @Path("/{type}")
    public Response getSessions(@PathParam("type") final SessionType type) {
        return wrapJWTRefresh(ResourceServerConnector.getSession(this.claim.userId, type));
    }

    @POST
    @Path("/{type}")
    public Response upload(@PathParam("type") final SessionType type,
            @QueryParam("timestamp") final long timestamp,
            @HeaderParam(HttpHeaders.X_NFC_RESPONSE) final String nfcResponse,
            final InputStream requestStream) {
        Response response = ResourceServerConnector.postSession(this.claim.userId, type, timestamp,
                nfcResponse, requestStream);
        if (response.getStatusInfo() != Response.Status.CREATED) {
            response = Response.fromResponse(response).status(Response.Status.CREATED).build();
        }
        return wrapJWTRefresh(response);
    }

    public Response wrapJWTRefresh(Response response) throws WebException {
        try {
            return Response.fromResponse(response)
                    .header(HttpHeaders.SET_AUTHORIZATION, TokenUtils.createJWT(this.claim)).build();
        } catch (InvalidKeyException | JsonProcessingException e) {
            throw new WebException(e);
        }
    }
}
