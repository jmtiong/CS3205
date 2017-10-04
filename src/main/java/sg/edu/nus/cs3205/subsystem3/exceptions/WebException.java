package sg.edu.nus.cs3205.subsystem3.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import sg.edu.nus.cs3205.subsystem3.objects.ErrorResponse;

public class WebException extends WebApplicationException {
    private static final long serialVersionUID = -3081261969780084208L;

    public WebException() {
        super();
    }

    public WebException(Throwable cause) {
        super(cause);
    }

    public WebException(Throwable cause, Response response) {
        super(cause, response);
    }

    public WebException(Throwable cause, Status status, String errorMessage) {
        this(cause, Response.status(status).entity(new ErrorResponse(errorMessage)).type(MediaType.APPLICATION_JSON)
                .build());
    }

    public WebException(Status status, String errorMessage) {
        this(null, status, errorMessage);
    }

    public WebException(Status status, String errorMessageFormat, Object... formatArgs) {
        this(status, String.format(errorMessageFormat, formatArgs));
    }

    public WebException(Status status, Exception exception) {
        this(exception, status, exception.getMessage());
    }
}
