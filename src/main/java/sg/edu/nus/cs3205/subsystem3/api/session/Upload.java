package sg.edu.nus.cs3205.subsystem3.api.session;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
public class Upload implements Session {
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

    @Path("/upload/test")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response testingUploadFileByClient(){
      String path = "/location.json";
      File f = new File(path);
      InputStream stream = null;
      try{
        stream = new FileInputStream(f);
      }catch(Exception e){
        e.printStackTrace();
      }
      Client client = ClientBuilder.newClient();
      Invocation.Builder builder = client.target("<location endpoint>").request();
      Response response = builder.post(Entity.entity(stream, "application/json"));
      return Response.status(200).entity("tesing only")
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
          .build();
    }
}
