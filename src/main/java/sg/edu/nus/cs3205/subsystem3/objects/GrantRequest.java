package sg.edu.nus.cs3205.subsystem3.objects;

public class GrantRequest {
    public GrantType grantType;
    public Integer userId;
    public String username;
    public String passhash;

    @SuppressWarnings("unused")
    private static final class GrantClaim {
        public int userId;
        public long exp;

        public GrantClaim(int userId, long expiration) {
            this.userId = userId;
            this.exp = expiration;
        }
    }

    // TODO: handle JsonMappingException when inputting an integer out of range
    public static enum GrantType {
        PASSWORD;
    }

    public GrantClaim getPasswordClaim(long expiration) {
        if (userId == null) {
            throw new IllegalStateException("userId hasn't been populated");
        }
        return new GrantClaim(userId, expiration);
    }
}
