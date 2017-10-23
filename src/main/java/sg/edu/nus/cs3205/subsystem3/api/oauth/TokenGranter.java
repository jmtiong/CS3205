package sg.edu.nus.cs3205.subsystem3.api.oauth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.objects.GrantRequest;
import sg.edu.nus.cs3205.subsystem3.objects.GrantRequest.GrantType;
import sg.edu.nus.cs3205.subsystem3.objects.PasswordGrant;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

import sg.edu.nus.cs3205.subsystem3.dao.DB;


// Challenge Response Implementation imports
import java.util.Base64;
import java.util.Arrays;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TokenGranter {
    @GET
    public Response get() {
        return Response.ok(new Object() {
            @SuppressWarnings("unused")
            public GrantType[] supportedGrantTypes = GrantRequest.GrantType.values();
        }).build();
    }

    @POST
    public Response grant(final GrantRequest request, @HeaderParam("X-NFC-Token") final String nfcToken, @HeaderParam("Authorization") final String authorizationHeader) {
        if (request == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing request body");
        }
        if (request.grantType == null) {
            throw new WebException(Response.Status.BAD_REQUEST, "Missing/invalid grant_type");
        } else if (request.grantType == GrantType.PASSWORD) {
            if (request.username != null && authorizationHeader == null) {
                int status = 401;
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode jObj = mapper.createObjectNode();
                // get challenge and salt from server 4
                String challenge = getChallenge(request.username);
                String salt = getUserSalt(request.username);
                jObj.put("challenge", challenge);
                jObj.put("salt", salt);
                if(challenge.isEmpty() || salt.isEmpty()){
                  return Response.status(status).entity("Server unable to generate challenge for unknown user.").build();
                }
                // Return back www-authenticate header
                try{
                  return Response.status(status)
                          .header("www-authenticate", mapper.writeValueAsString(jObj))
                          .entity("Missing Authorization Header.").build();
                } catch(Exception e){
                  e.printStackTrace();
                  status = 500;
                }
                return Response.status(status).entity("Server unable to generate challenge for unknown user.").build();
            }
            // User attempts to log in
            if (request.username != null && authorizationHeader != null) {
              if (nfcToken == null) {
                  throw new WebException(Response.Status.BAD_REQUEST, "Missing X-NFC-Token header");
              }
              String[] authHeader = authorizationHeader.split(" ");
              if(authHeader.length < 2){
                throw new WebException(Response.Status.BAD_REQUEST, "Invalid Authorization Header.");
              }
                boolean answer = loginUser(request.username, authorizationHeader, nfcToken);
                if(answer){
                  try {
                      request.userId = 1;
                      final String jwt = TokenUtils.createJWT(request);
                      return Response.ok(new PasswordGrant(jwt)).build();
                  } catch (JsonProcessingException | InvalidKeyException e) {
                      throw new WebException(e);
                  }
                }
            }
            throw new WebException(Response.Status.UNAUTHORIZED, "Invalid credential");
        }
        throw new WebException(Response.Status.BAD_REQUEST, "Unknown %s grant_type", request.grantType);
    }

    private static final String RESOURCE_SERVER_SESSION_PATH = "http://cs3205-4-i.comp.nus.edu.sg/api/team3";

    private String getUserSalt(String username){
      final String target;
          target = String.format("%s/%s?username=%s", RESOURCE_SERVER_SESSION_PATH,
                  "user", username, "salt2");
      final Invocation.Builder client = ClientBuilder.newClient().target(target).request();
      System.out.println("GET " + target);
      // TODO Add in the headers for server 4 verification in the future
      final Response response = client.get();
      String rawResponse = response.readEntity(String.class);
      if(response.getStatus() == 401) {
        rawResponse = "";
      }
      return rawResponse;
    }

    private String getChallenge(String username){
      final String target;
          target = String.format("%s/%s/%s?username=%s", RESOURCE_SERVER_SESSION_PATH,
                  "user", "challenge", username);
      final Invocation.Builder client = ClientBuilder.newClient().target(target).request();
      System.out.println("GET " + target);
      final Response response = client.get();
      String rawResponse = response.readEntity(String.class);
      if(response.getStatus() == 401) {
        rawResponse = "";
      }
      return rawResponse;
    }

    private boolean loginUser(String username, String authorizationHeader, String nfcToken){
      System.out.println(username+" "+authorizationHeader+" "+nfcToken);
      final String target;
          target = String.format("%s/%s/%s?username=%s", RESOURCE_SERVER_SESSION_PATH,
                  "user", "login", username);
      final Invocation.Builder client = ClientBuilder.newClient().target(target).request();
      System.out.println("POST " + target);
      final Response response = client
                                  .header("Authorization", authorizationHeader)
                                  .header("X-NFC-Token", nfcToken)
                                  .post(null);
      if(response.getStatus() == 201){
        return true;
      }
      return false;

    }
}
