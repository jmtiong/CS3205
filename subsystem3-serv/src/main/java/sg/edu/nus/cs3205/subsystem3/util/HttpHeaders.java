package sg.edu.nus.cs3205.subsystem3.util;

public interface HttpHeaders extends javax.ws.rs.core.HttpHeaders {
    String CONNECTION = "Connection";
    String X_NFC_CHALLENGE = "X-NFC-Challenge";
    String X_NFC_RESPONSE = "X-NFC-Response";
    String X_PASSWORD_RESPONSE = "X-Password-Response";
    String SET_AUTHORIZATION = "Set-Authorization";
}
