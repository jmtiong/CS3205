package sg.edu.nus.cs3205.subsystem3;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import sg.edu.nus.cs3205.subsystem3.api.session.HeartSession;

/**
 * Hello world!
 *
 */
@Path("/")
public class Server {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String getIndex() {
        return "\"Hello World!\"";
    }

    @Path("/upload")
    public HeartSession upload() {
        return new HeartSession();
    }
}
