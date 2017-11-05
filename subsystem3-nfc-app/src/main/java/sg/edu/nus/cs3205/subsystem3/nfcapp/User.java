package sg.edu.nus.cs3205.subsystem3.nfcapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public static final String[] FIELDS = { "Username", "First Name", "Last Name", "NRIC", "Gender",
            "Address", "DOB" };
    public String username;
    public String firstname;
    public String lastname;
    public String nric;
    public String gender;
    public String address;
    public String dob;

    public void setAddress(String[] addressLines) {
        this.address = addressLines[0];
    }

    public String get(int col) {
        if (col == 0) {
            return username;
        } else if (col == 1) {
            return firstname;
        } else if (col == 2) {
            return lastname;
        } else if (col == 3) {
            return nric;
        } else if (col == 4) {
            return gender;
        } else if (col == 5) {
            return address;
        } else {
            return dob;
        }
    }
}
