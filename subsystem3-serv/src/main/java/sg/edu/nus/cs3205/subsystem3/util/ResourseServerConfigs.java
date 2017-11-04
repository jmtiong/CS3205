package sg.edu.nus.cs3205.subsystem3.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourseServerConfigs {
    private static final String BUNDLE_NAME = "sg.edu.nus.cs3205.subsystem3.util.resourceserverconfigs";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private ResourseServerConfigs() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getBasicAuthorization() {
        return getString("resourceserver.basicauthorization");
    }

    public static String getKeyStore() {
        return getString("ssl.keystore");
    }

    public static String getTrustStore() {
        return getString("ssl.truststore");
    }

    public static String getSSLPassword() {
        return getString("ssl.password");
    }
}
