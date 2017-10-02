package sg.edu.nus.cs3205.subsystem3.objects;

public class GrantRequest {
    public GrantType grantType;
    public String username;
    public String passhash;

    // TODO: handle JsonMappingException when inputting an integer out of range
    public static enum GrantType {
        PASSWORD;
    }

    public String getPasswordClaim(long expiration) {
        return String.format("{\"username\":\"%s\",\"exp\":%d}", username, expiration);
    }
}
