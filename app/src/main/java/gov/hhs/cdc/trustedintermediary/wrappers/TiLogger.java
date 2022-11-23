package gov.hhs.cdc.trustedintermediary.wrappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Humble object interface for logging This class was created in order to take away the 3rd party
 * dependency of the logger. The idea is to have the logger dependency only in this class. If there
 * ever is a reason to use a different logger, then we only need to make the changes here.
 */
public class TiLogger implements MyLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("tilogger");

    // ANSI escape code
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private TiLogger() {}

    @Override
    public void logInfo(String infoMessage) {
        LOGGER.info(ANSI_GREEN + infoMessage + ANSI_RESET);
    }

    @Override
    public void logWarning(String warningMessage) {
        LOGGER.warn(ANSI_YELLOW + warningMessage + ANSI_RESET);
    }

    @Override
    public void logDebug(String debugMessage) {
        LOGGER.debug(ANSI_CYAN + debugMessage + ANSI_RESET);
    }

    @Override
    public void logError(String errorMessage) {
        LOGGER.error(ANSI_RED + errorMessage + ANSI_RESET);
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        LOGGER.debug(ANSI_CYAN + debugMessage + ANSI_RESET, e);
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        LOGGER.error(ANSI_RED + errorMessage + ANSI_RESET, e);
    }

    public static TiLogger getLogger() {
        return new TiLogger();
    }
}
