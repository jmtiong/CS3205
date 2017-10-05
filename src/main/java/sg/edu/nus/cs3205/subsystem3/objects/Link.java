package sg.edu.nus.cs3205.subsystem3.objects;

public class Link {
    public String link;
    public String rel;
    public String type;

    public Link(String link, String rel, String type) {
        this.link = link;
        this.rel = rel;
        this.type = type;
    }
}