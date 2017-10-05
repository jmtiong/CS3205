package sg.edu.nus.cs3205.subsystem3.dao;

public class UserDO {
    private int uid;
    private final String username;
    private String nfcid;

    public UserDO(final String username) {
        super();
        this.username = username;
    }

    public String getNfcid() {
        return this.nfcid;
    }

    public void setNfcid(final String nfcid) {
        this.nfcid = nfcid;
    }

    public int getUid() {
        return this.uid;
    }

    public String getUsername() {
        return this.username;
    }

}
