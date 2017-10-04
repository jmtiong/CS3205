package sg.edu.nus.cs3205.subsystem3.util.security;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TokenConfigs {
    private static final String BUNDLE_NAME = "sg.edu.nus.cs3205.subsystem3.util.security.token"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private TokenConfigs() {
    }

    public static String getConfig(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
