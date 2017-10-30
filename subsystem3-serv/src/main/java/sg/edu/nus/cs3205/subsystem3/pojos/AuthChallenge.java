package sg.edu.nus.cs3205.subsystem3.pojos;

public class AuthChallenge {
    public String challenge;
    public String salt;

    public AuthChallenge(String challenge, String salt) {
        this.challenge = challenge;
        this.salt = salt;
    }
}
