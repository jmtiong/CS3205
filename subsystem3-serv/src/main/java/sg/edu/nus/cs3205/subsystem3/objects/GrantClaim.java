package sg.edu.nus.cs3205.subsystem3.objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class GrantClaim {
    public Integer userId;
    public Long exp;

    public GrantClaim() {
    }

    public GrantClaim(final int userId, final long expiration) {
        this.userId = userId;
        this.exp = expiration;
    }
}
