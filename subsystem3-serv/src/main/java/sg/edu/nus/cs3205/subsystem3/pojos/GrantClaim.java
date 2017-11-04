package sg.edu.nus.cs3205.subsystem3.pojos;

public class GrantClaim {
    public Integer userId;
    public String username;
    public Long exp;

    public GrantClaim() {
    }

    public GrantClaim(final int userId, final String username, final long expiration) {
        this.userId = userId;
        this.username = username;
        this.exp = expiration;
    }
}
