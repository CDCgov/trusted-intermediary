package gov.hhs.cdc.trustedintermediary;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;

public class ApiConfig {
    private static final String PROPERTIES_FILE_NAME = "api.properties";
    private static Properties properties = new Properties();

    @Inject Logger logger;

    static {
        try {
            InputStream inputStream =
                    ApiConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
            properties.load(inputStream);
        } catch (IOException e) {
            System.out.println("Failed to load property file due to: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
