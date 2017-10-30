package sg.edu.nus.cs3205.subsystem3.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import sg.edu.nus.cs3205.subsystem3.pojos.ErrorResponse;

public class WebException extends WebApplicationException {
    private static final long serialVersionUID = -3081261969780084208L;

    public WebException() {
        super();
    }

    public WebException(final Throwable cause) {
        super(cause);
    }

    public WebException(final Throwable cause, final Response response) {
        super(cause, response);
    }

    public WebException(final Throwable cause, final Status status, final String errorMessage) {
        this(cause, Response.status(status).entity(new ErrorResponse(errorMessage))
                .type(MediaType.APPLICATION_JSON_TYPE).build());
    }

    public WebException(final Status status, final String errorMessage) {
        this(null, status, errorMessage);
    }

    public WebException(final Status status, final String errorMessageFormat, final Object... formatArgs) {
        this(status, String.format(errorMessageFormat, formatArgs));
    }

    public WebException(final Status status, final Exception exception) {
        this(exception, status, exception.getMessage());
    }
}
