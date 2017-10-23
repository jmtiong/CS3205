package sg.edu.nus.cs3205.subsystem3.objects;

import javax.ws.rs.core.UriInfo;

public final class Links {
    public Link[] links;

    public Links(final Link... links) {
        this.links = links;
    }

    public static Link newLink(final UriInfo uri, final String path, final String rel, final String type) {
        return new Link(uri.getAbsolutePathBuilder().path(path).build().toString(), rel, type);
    }
}
