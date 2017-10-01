package sg.edu.nus.cs3205.subsystem3.util.security;

import java.nio.charset.StandardCharsets;
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

    public static <T> T getConfig(String key, Class<T> clazz) {
        if (clazz == byte[].class) {
            return clazz.cast(getConfig(key).getBytes(StandardCharsets.UTF_8));
        } else if (clazz == long.class || clazz == Long.class) {
            return clazz.cast(Long.valueOf(getConfig(key)));
        } else {
            return clazz.cast(getConfig(key));
        }
    }
}
