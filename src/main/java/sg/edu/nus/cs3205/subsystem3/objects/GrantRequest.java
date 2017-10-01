package sg.edu.nus.cs3205.subsystem3.objects;

public class GrantRequest {
    private GrantType grantType;
    private String username;
    private String passhash;

    public static enum GrantType {
        PASSWORD;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(GrantType grantType) {
        this.grantType = grantType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    public String getPasswordClaim(long expiration) {
        return String.format("{\"username\":\"%s\",\"exp\":%d}", username, expiration);
    }
}
