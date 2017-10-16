package sg.edu.nus.cs3205.subsystem3.api.oauth;

import java.security.InvalidKeyException;

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

import sg.edu.nus.cs3205.subsystem3.util.Challenge;

import sg.edu.nus.cs3205.subsystem3.dao.DB;


// Challenge Response Implementation imports
import java.util.Base64;
import java.util.Arrays;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// import javax.ws.rs.client.ClientBuilder;
// import javax.ws.rs.client.Entity;
// import javax.ws.rs.client.Invocation;

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
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode jObj = mapper.createObjectNode();
                // Generate challenge
                byte[] challenge = Challenge.generateChallenge();
                // Store challenge to server 3 local database
                if(storeChallenge(challenge, request.username) >= 1){
                  // Put challenge to jObj
                  jObj.put("challenge", Base64.getEncoder().encodeToString(challenge));
                  // Obtain salt from server 4
                  jObj.put("salt", "getUserSalt(request.username)");
                  try{
                    return Response.status(401)
                            .header("WWW-AUTHENTICATE", mapper.writeValueAsString(jObj))
                            .entity("Missing Authorization Header.").build();
                  } catch(Exception e){
                    e.printStackTrace();
                  }
                }
                return Response.status(500).entity("Server unable to generate challenge.").build();
            }
            if (request.username != null && authorizationHeader != null) {
              if (nfcToken == null) {
                  throw new WebException(Response.Status.BAD_REQUEST, "Missing X-NFC-Token header");
              }
              System.out.println(authorizationHeader+" authorizationHeader:");
              String[] authHeader = authorizationHeader.split(" ");
              if(authHeader.length < 2){
                throw new WebException(Response.Status.BAD_REQUEST, "Invalid Authorization Header.");
              }
              byte[] response = Base64.getDecoder().decode(authHeader[1].getBytes());
              byte[] challenge = getUserChallenge(request.username);
              // get from server 4
              byte[] passwordHash = null;
              if(Challenge.validateResponse(response, challenge, passwordHash)){
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

    private int storeChallenge(byte[] challenge, String username){
      String sql = "SELECT uid FROM CS3205.user WHERE username = ?";
      int result = 0;
        try{
            // Maybe need to get from server 4
            PreparedStatement ps = DB.connectDatabase().prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
              int userID = rs.getInt("uid");
              sql = "INSERT INTO CS3205.challenge (challengeString, userID) VALUES (?, ?) ON DUPLICATE KEY UPDATE challengeString=VALUES(challengeString)";
              ps = DB.connectDatabase().prepareStatement(sql);
              System.out.println("String: "+Base64.getEncoder().encodeToString(challenge) + "   "+Arrays.toString(challenge));
              ps.setString(1, Base64.getEncoder().encodeToString(challenge));
              ps.setInt(2, userID);
              result = ps.executeUpdate();
              break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private byte[] getUserChallenge(String username){
      String sql = "SELECT * FROM CS3205.user WHERE username = ?";
        try{
            PreparedStatement ps = DB.connectDatabase().prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
              int userID = rs.getInt("uid");
              sql = "SELECT * FROM CS3205.challenge WHERE userID = ?";
              ps = DB.connectDatabase().prepareStatement(sql);
              ps.setInt(1, userID);
              rs = ps.executeQuery();
              while(rs.next()){
                return Base64.getDecoder().decode(rs.getString("challengeString").getBytes());
              }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static final String RESOURCE_SERVER_SESSION_PATH = "http://cs3205-4-i.comp.nus.edu.sg/api/team3/";

}
