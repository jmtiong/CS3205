package sg.edu.nus.cs3205.subsystem3.api.session;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public interface Session {
    public Response upload(String type, long timestamp, final InputStream is);

    public String get();
}
