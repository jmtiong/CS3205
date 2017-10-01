package sg.edu.nus.cs3205.subsystem3.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class WebException extends WebApplicationException {
    private static final long serialVersionUID = -3081261969780084208L;

    static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    public WebException() {
        super();
    }

    public WebException(Status status, String errorMessage) {
        super(Response.status(status).entity(new ErrorResponse(errorMessage)).build());
    }

    public WebException(Throwable e) {
        super(e);
    }
}
