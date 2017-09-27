package sg.edu.nus.cs3205.subsystem3.api.session;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import sg.edu.nus.cs3205.subsystem3.api.session.core.Session;

@Path("/heart")
public class HeartSession implements Session {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Override
    public String get() {
        return "test";
    }

    @POST
    @Path("/upload")
    @Produces(MediaType.TEXT_PLAIN)
    @Override
    public void upload() {
        System.out.println("Testing");
    }
}
