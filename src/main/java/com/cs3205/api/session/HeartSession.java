package cs3205.api.session;

import cs3205.api.session.core.Session;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/heart")
public class HeartSession implements Session{
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Override
  public String get(){
    return "test";
  }

  @POST
  @Path("/upload")
  @Produces(MediaType.TEXT_PLAIN)
  @Override
  public void upload(){
    System.out.println("Testing");
  }
}
