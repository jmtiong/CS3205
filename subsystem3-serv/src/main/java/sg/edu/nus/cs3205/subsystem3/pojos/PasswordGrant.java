package sg.edu.nus.cs3205.subsystem3.pojos;

public class PasswordGrant {
    public final String tokenType = "Bearer";
    public String accessToken;

    public PasswordGrant(final String jwt) {
        this.accessToken = jwt;
    }

}
