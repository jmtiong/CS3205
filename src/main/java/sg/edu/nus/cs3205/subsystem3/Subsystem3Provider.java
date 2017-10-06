package sg.edu.nus.cs3205.subsystem3;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import sg.edu.nus.cs3205.subsystem3.objects.ErrorResponse;

@Provider
public class Subsystem3Provider extends JacksonJsonProvider
        implements ExceptionMapper<JsonProcessingException> {
    public Subsystem3Provider() {
        super(new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));
    }

    @Override
    public Response toResponse(final JsonProcessingException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
