package sg.edu.nus.cs3205.subsystem3.api.session;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public class Upload implements Session {
    int userID = 0;
    public Upload(int userID){
      this.userID = userID;
    }
    @GET
    @Override
    public String get() {
        return "\"You sent a GET request\"";
    }

    @Path("/{type}/{int}/{timestamp}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response upload(@PathParam("type")String type, @PathParam("timestamp")long timestamp, @PathParam("int")int heartrate){
      // Verify jWTToken
      // Obtain userID from the token

      Response response = null;
      InputStream stream = null;
      if(type.equalsIgnoreCase("heart")){
        response = uploadToHeart(userID, heartrate, timestamp);
      }else{
        response = Response.status(401).entity("unknown request made.").build();
      }

      return response;
    }
    @Path("/{type}/{timestamp}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response upload(@PathParam("type")String type, @PathParam("timestamp")long timestamp, final InputStream is){
      // Verify jWTToken
      // Obtain userID from the token

      Response response = null;
      InputStream stream = null;
      if(type.equalsIgnoreCase("image")){
        response = uploadToImage(userID, is, timestamp);
      }else if(type.equalsIgnoreCase("video")){
        response = uploadToImage(userID, is, timestamp);
      }else if(type.equalsIgnoreCase("step")){
        response = uploadToImage(userID, is, timestamp);
      }else{
        response = Response.status(401).entity("unknown request made.").build();
      }

        return response;
    }

    private Response uploadToStep(int userID, InputStream stream, long timestamp) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder builder = client.target(
                "http://cs3205-4-i.comp.nus.edu.sg/api/team3/steps/" + userID + "/upload/" + timestamp)
                .request();
        // @TODO: Add in the headers for server 4 verification in the future
        Response response = builder.post(Entity.entity(stream, "application/json"));
        return response;
    }

    private Response uploadToImage(int userID, InputStream stream, long timestamp) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder builder = client.target(
                "http://cs3205-4-i.comp.nus.edu.sg/api/team3/image/" + userID + "/upload/" + timestamp)
                .request();
        // @TODO: Add in the headers for server 4 verification in the future
        Response response = builder.post(Entity.entity(stream, "image/jpeg"));
        return response;
    }

    private Response uploadToVideo(int userID, InputStream stream, long timestamp) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder builder = client.target(
                "http://cs3205-4-i.comp.nus.edu.sg/api/team3/video/" + userID + "/upload/" + timestamp)
                .request();
        // @TODO: Add in the headers for server 4 verification in the future
        Response response = builder.post(Entity.entity(stream, "video/mpeg"));
        return response;
    }

    private Response uploadToHeart(int userID, int heartrate, long timestamp) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder builder = client.target("http://cs3205-4-i.comp.nus.edu.sg/api/team3/heartservice/"
                + userID + "/" + heartrate + "/" + timestamp).request();
        // @TODO: Add in the headers for server 4 verification in the future
        Response response = builder.post(Entity.entity("a sample text", "text/plain"));
        return response;
    }

    // public Response testingUploadFileByClient() {
    // String path = "/location.json";
    // File f = new File(path);
    // InputStream stream = null;
    // try {
    // stream = new FileInputStream(f);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // Client client = ClientBuilder.newClient();
    // Invocation.Builder builder = client.target("<location
    // endpoint>").request();
    // Response response = builder.post(Entity.entity(stream,
    // "application/json"));
    // return Response.status(200).entity("tesing
    // only").header("Access-Control-Allow-Origin", "*")
    // .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT,
    // OPTIONS").build();
}
