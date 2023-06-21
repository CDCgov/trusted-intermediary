package gov.hhs.cdc.trustedintermediary;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;

public class ApiConfig {
    private static final ApiConfig INSTANCE = new ApiConfig();
    private static final String PROPERTIES_FILE_NAME = "api.properties";
    private Properties properties;

    @Inject Logger logger;

    private ApiConfig() {
        properties = new Properties();
        try {
            InputStream inputStream =
                    ApiConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
            properties.load(inputStream);
        } catch (IOException e) {
            logger.logFatal("Failed to load property file", e);
        }
    }

    public static ApiConfig getInstance() {
        return INSTANCE;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
