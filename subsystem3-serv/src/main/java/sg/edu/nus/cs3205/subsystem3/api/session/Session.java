package sg.edu.nus.cs3205.subsystem3.api.session;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import sg.edu.nus.cs3205.subsystem3.pojos.Link;
import sg.edu.nus.cs3205.subsystem3.pojos.Links;

@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_JSON)
public class Session {
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

    private static final String RESOURCE_SERVER_SESSION_PATH = "http://cs3205-4-i.comp.nus.edu.sg/api/team3";

    private final Integer userID;

    public Session(final int userID) {
        this.userID = userID;
    }

    @GET
    public Response getRoot(@Context final UriInfo uri) {
        return Response
                .ok(new Links(Stream
                        .concat(Stream.of(Links.newLink(uri, "", "self", HttpMethod.GET)),
                                Stream.of(SessionType.values())
                                        .map(type -> Links.newLink(uri, type.toString(), "session." + type,
                                                HttpMethod.GET + ',' + HttpMethod.POST)))
                        .toArray(Link[]::new)))
                .build();
    }

    @GET
    @Path("/{type}")
    public Response getSessions(@PathParam("type") final SessionType type) {
        final String target = String.format("%s/%s/%d/all", RESOURCE_SERVER_SESSION_PATH, type, this.userID);
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .request(MediaType.APPLICATION_JSON_TYPE);
        System.out.println(HttpMethod.GET + ' ' + target);
        return client.get();
    }

    @POST
    @Path("/{type}")
    public Response upload(@PathParam("type") final SessionType type,
            @QueryParam("timestamp") final long timestamp, final InputStream requestStream) {
        final String target = String.format("%s/%s/%d/upload/%d", RESOURCE_SERVER_SESSION_PATH, type,
                this.userID, timestamp);
        final Invocation.Builder client = ClientBuilder.newClient().target(target).request();
        System.out.println(HttpMethod.POST + ' ' + target);
        // TODO Add in the headers for server 4 verification in the future
        final Response response = client
                .post(Entity.entity(requestStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        // TODO Custom response
        return response;
    }
}
