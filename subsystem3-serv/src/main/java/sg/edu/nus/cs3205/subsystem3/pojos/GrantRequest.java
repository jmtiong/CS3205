package sg.edu.nus.cs3205.subsystem3.pojos;

public class GrantRequest {
    public GrantType grantType;
    public Integer userId;
    public String username;

    public static enum GrantType {
        PASSWORD;
    }

    public GrantClaim getPasswordClaim(final long expiration) {
        if (this.userId == null) {
            throw new IllegalStateException("userId hasn't been populated");
        }
        return new GrantClaim(this.userId, this.username, expiration);
    }
}
