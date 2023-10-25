package gov.hhs.cdc.trustedintermediary.context;

import io.github.cdimascio.dotenv.Dotenv;

class DotEnv {
    private static Dotenv DOTENV;

    private DotEnv() {}

    public static String get(String key) {
        return DOTENV.get(key);
    }

    public static String get(String key, String defaultValue) {
        return DOTENV.get(key, defaultValue);
    }

    public static void load() {
        DOTENV = Dotenv.configure().ignoreIfMissing().load();
    }

    public static void load(Dotenv dotenv) {
        DOTENV = dotenv;
    }
}
