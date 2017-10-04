package sg.edu.nus.cs3205.subsystem3;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.nus.cs3205.subsystem3.api.oauth.TokenGranter;
import sg.edu.nus.cs3205.subsystem3.api.session.HeartSession;
import sg.edu.nus.cs3205.subsystem3.api.session.Upload;
import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Server {
    @Context
    UriInfo uri;

    @GET
    public Response getRoot() {
        return Response.ok(new Object() {
            class Link {
                @SuppressWarnings("unused")
                public String rel, href;

                public Link(String r, String h) {
                    rel = r;
                    href = h;
                }
            }

            @SuppressWarnings("unused")
            public Link[] links = new Link[] { new Link("self", uri.getBaseUri() + ""),
                    new Link("oauth.token", uri.getBaseUri() + "oauth/token"),
                    new Link("upload", uri.getBaseUri() + "upload") };
        }).build();
    }

    @Path("/oauth/token")
    public TokenGranter grant() {
        return new TokenGranter();
    }

    @Path("/upload")
    public Upload upload(@HeaderParam("Authorization") String accessToken,
            @HeaderParam("X-NFC-Token") String nfcToken) {
        if (accessToken == null) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Missing Authorization header");
        } else if (!accessToken.startsWith("Bearer ")) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Bearer access token required");
        }
        try {
            TokenUtils.verifyJWT(accessToken.substring("Bearer ".length()));
        } catch (Exception e) {
            throw new WebException(Response.Status.UNAUTHORIZED, e);
        }

        if (nfcToken == null) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Missing X-NFC-Token header");
        }
        try {
            nfcToken.charAt(0);
        } catch (Exception e) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Invalid NFC token");
        }

        // All is well, obtain username from the token
        String[] parts = accessToken.split("\\.");
        String jsonStr = TokenUtils.decodeString(parts[1]);
        JsonNode jn = null;
        ObjectMapper mapper = new ObjectMapper();
        try{
           jn = mapper.readValue(jsonStr, JsonNode.class);
        } catch(Exception e){
          e.printStackTrace();
        }
        if(jn == null){
         throw new WebException(Response.Status.UNAUTHORIZED, "Invalid token values");
        }
        // @TODO: CHANGE SOON (into UserID)
        int userID = jn.path("username").asInt();
        return new Upload(userID);
    }
}
