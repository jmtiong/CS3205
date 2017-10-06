package sg.edu.nus.cs3205.subsystem3.api.session;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
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

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.objects.Link;
import sg.edu.nus.cs3205.subsystem3.objects.Links;

@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_JSON)
public class Session {
    public static enum SessionType {
        HEART("heart", "heartservice"), STEP("step", "steps"), IMAGE("image"), VIDEO("video");

        public String pathPrefix;
        public String resourceServerPath;

        private SessionType(final String path) {
            this(path, path);
        }

        private SessionType(final String pathPrefix, final String resourceServerPath) {
            this.pathPrefix = pathPrefix;
            this.resourceServerPath = resourceServerPath;
        }

        public static SessionType fromString(final String value) {
            try {
                return Stream.of(SessionType.values()).filter(type -> value.startsWith(type.pathPrefix))
                        .findAny().get();
            } catch (NullPointerException | NoSuchElementException e) {
                throw new IllegalArgumentException(String.format("Invalid session type %s in path", value));
            }
        }

        @Override
        public String toString() {
            return this.pathPrefix;
        }
    }

    private static final String RESOURCE_SERVER_SESSION_PATH = "http://cs3205-4-i.comp.nus.edu.sg/api/team3";

    private final Integer userID;

    public Session(final int userID) {
        this.userID = userID;
    }

    @GET
    public Response get(@Context final UriInfo uri) {
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
    public Response get(@PathParam("type") final SessionType type) {
        String target = String.format("%s/%s/%d/all", RESOURCE_SERVER_SESSION_PATH, type.resourceServerPath,
                this.userID);
        final Invocation.Builder client = ClientBuilder.newClient().target(target)
                .request(MediaType.APPLICATION_JSON_TYPE);
        System.out.println(HttpMethod.GET + ' ' + target);
        return client.get();
    }

    @POST
    @Path("/{type}")
    public Response upload(@PathParam("type") final SessionType type,
            @QueryParam("timestamp") final long timestamp, final InputStream requestStream) {
        final String target;
        if (type == SessionType.HEART) {
            final Scanner scanner = new Scanner(requestStream);
            if (!scanner.hasNext()) {
                scanner.close();
                throw new WebException(Response.Status.BAD_REQUEST, "Provide heart rate in body");
            } else if (!scanner.hasNextInt()) {
                scanner.close();
                throw new WebException(Response.Status.BAD_REQUEST, "Heart rate should be an integer");
            }
            target = String.format("%s/%s/%d/%d/%d", RESOURCE_SERVER_SESSION_PATH, type.resourceServerPath,
                    this.userID, scanner.nextInt(), timestamp);
            scanner.close();
        } else {
            target = String.format("%s/%s/%d/upload/%d", RESOURCE_SERVER_SESSION_PATH,
                    type.resourceServerPath, this.userID, timestamp);
        }
        final Invocation.Builder client = ClientBuilder.newClient().target(target).request();
        System.out.println(HttpMethod.POST + ' ' + target);
        // TODO Add in the headers for server 4 verification in the future
        final Response response = client.post(Entity.entity(type == SessionType.HEART ? null : requestStream,
                MediaType.APPLICATION_OCTET_STREAM_TYPE));
        // TODO Custom response
        return response;
    }

    @Deprecated
    @POST
    @Path("/{type}/{timestamp}")
    public Response upload2(@PathParam("type") final SessionType type,
            @PathParam("timestamp") final long timestamp, final InputStream requestStream) {
        return this.upload(type, timestamp, requestStream);
    }
}
