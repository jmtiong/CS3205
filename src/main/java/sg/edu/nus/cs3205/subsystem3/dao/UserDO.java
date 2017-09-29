package sg.edu.nus.cs3205.subsystem3.dao;

public class UserDO {
    private int uid;
    private String username;
    private String nfcid;

    public UserDO(String username) {
        super();
        this.username = username;
    }

    public String getNfcid() {
        return nfcid;
    }

    public void setNfcid(String nfcid) {
        this.nfcid = nfcid;
    }

    public int getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

}
