package gov.hhs.cdc.trustedintermediary.context;

import io.github.cdimascio.dotenv.Dotenv;

class DotEnv {
    private static final Dotenv DOTENV = Dotenv.load();

    private DotEnv() {}

    public static String get(String key) {
        return DOTENV.get(key);
    }

    public static boolean isNull() {
        return DOTENV == null;
    }
}
