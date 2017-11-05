package sg.edu.nus.cs3205.subsystem3.api;

import javax.ws.rs.core.Response;

import sg.edu.nus.cs3205.subsystem3.api.oauth.TokenGranter;
import sg.edu.nus.cs3205.subsystem3.api.session.ISession;
import sg.edu.nus.cs3205.subsystem3.api.session.Session;
import sg.edu.nus.cs3205.subsystem3.api.session.UnauthorizedSession;
import sg.edu.nus.cs3205.subsystem3.exceptions.WebException;
import sg.edu.nus.cs3205.subsystem3.pojos.GrantClaim;
import sg.edu.nus.cs3205.subsystem3.util.security.TokenUtils;

public class Authenticator {
    private static final String BEARER_AND_SPACE = "Bearer ";

    public static TokenGranter authenticateTokenGranter() {
        return new TokenGranter();
    }

    public static ISession authenticateSession(final String accessToken, final String nfcResponse) {
        GrantClaim claim;
        if (accessToken == null) {
            throw new WebException(Response.Status.UNAUTHORIZED, "Missing Authorization header");
        } else if (!accessToken.startsWith(BEARER_AND_SPACE)) {
            throw new WebException(Response.Status.UNAUTHORIZED, BEARER_AND_SPACE + "access token required");
        }

        try {
            claim = TokenUtils.verifyJWT(accessToken.substring(BEARER_AND_SPACE.length()));
        } catch (final Exception e) {
            throw new WebException(Response.Status.UNAUTHORIZED, e);
        }

        if (nfcResponse == null) {
            return new UnauthorizedSession(claim);
        } else {
            return new Session(claim);
        }
    }
}
