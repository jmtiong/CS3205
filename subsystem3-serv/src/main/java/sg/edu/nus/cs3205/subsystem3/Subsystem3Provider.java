package sg.edu.nus.cs3205.subsystem3;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import sg.edu.nus.cs3205.subsystem3.pojos.ErrorResponse;

@Priority(Integer.MAX_VALUE)
@Provider
public class Subsystem3Provider extends JacksonJsonProvider
        implements ExceptionMapper<JsonMappingException>, ContainerRequestFilter {
    private static final Logger LOGGER = Logger.getLogger(Subsystem3Provider.class.getName());

    @Context
    private HttpServletRequest request;

    private static ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

    public Subsystem3Provider() {
        super(objectMapper);
        LOGGER.fine("Registered provider " + this.getClass().getName());
    }

    @Override
    public synchronized void filter(ContainerRequestContext requestContext) throws IOException {
        String remote = request.getRemoteHost();
        if (remote == null) {
            remote = request.getRemoteAddr();
        }
        String local = request.getLocalName();
        if (local == null) {
            local = request.getLocalAddr();
        }
        LOGGER.info(String.format("%s from %s:%d to %s:%d\n", new Date(), remote, request.getRemotePort(),
                local, request.getLocalPort()));

        Enumeration<String> headerNames = request.getHeaderNames();

        LOGGER.info(StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<String>() {
            @Override
            public String next() {
                return headerNames.nextElement();
            }

            @Override
            public boolean hasNext() {
                return headerNames.hasMoreElements();
            }
        }, Spliterator.ORDERED), false).map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining("; ")));
    }

    @Override
    public Response toResponse(final JsonMappingException exception) {
        LOGGER.log(Level.INFO, "JsonMappingException", exception);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Invalid body's JSON format")).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
