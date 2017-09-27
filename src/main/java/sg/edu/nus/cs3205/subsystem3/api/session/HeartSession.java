package sg.edu.nus.cs3205.subsystem3.api.session;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
public class HeartSession implements Session {
    @GET
    @Override
    public String get() {
        return "\"You sent a GET request\"";
    }

    @POST
    @Override
    public String upload(String body) {
        return "\"You sent " + body + '"';
    }
}
