package gov.hhs.cdc.trustedintermediary.context;

public class Environment {

    private Environment() {}

    public static String getProperty(String key) {
        if (DotEnv.isNull()) {
            return System.getenv(key);
        }
        return DotEnv.get(key);
    }

    public static String getEnvironmentType() {
        String environment = getProperty("ENV");
        if (environment == null || environment.isEmpty()) {
            return "local";
        }
        return environment;
    }
}
