package sg.edu.nus.cs3205.subsystem3.objects;

public class PasswordGrant {
    public final String tokenType = "Bearer";
    public String accessToken;

    public PasswordGrant(final String jwt) {
        this.accessToken = jwt;
    }

}
