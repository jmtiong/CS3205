package sg.edu.nus.cs3205.subsystem3.api.oauth;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
public class TokenGranter {
    abstract class Response {
    };

    @GET
    public Response get() {
        return new Response() {
            public String[] supportedGrantType = new String[] { "password" };
        };
    }

    @POST
    public String grant(String body) {
        return "\"You sent " + body + '"';
    }
}
