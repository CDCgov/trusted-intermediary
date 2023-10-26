package gov.hhs.cdc.trustedintermediary.context;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * The <code>DotEnv</code> class provides a simple interface for managing environment variables
 * using the Dotenv library. This utility is designed to load and retrieve environment variables
 * from a .env file located in the shared <code>src/main/resource</code> folder. If the .env file is
 * not found in the specified location, Dotenv will also check system variables.
 *
 * <p>To use this utility, you can retrieve environment variables by their keys using the <code>get
 * </code> methods provided. If a key is not found, you can specify a default value to return.
 *
 * <p>The <code>load</code> method is primarily intended for testing purposes, allowing for custom
 * Dotenv instances to be set. It can be useful when configuring specific environments for testing
 * scenarios.
 *
 * @see <a href="https://github.com/cdimascio/dotenv-java">Dotenv Java GitHub Repository</a>
 * @see io.github.cdimascio.dotenv.Dotenv
 */
class DotEnv {
    private static Dotenv dotEnv;

    static {
        dotEnv = Dotenv.configure().ignoreIfMissing().load();
    }

    private DotEnv() {}

    public static String get(String key) {
        return dotEnv.get(key);
    }

    public static String get(String key, String defaultValue) {
        return dotEnv.get(key, defaultValue);
    }

    // Leave with default scope
    static void load(Dotenv dotenv) {
        dotEnv = dotenv;
    }
}
