package gov.hhs.cdc.trustedintermediary.external.slf4j;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Arrays;
import org.slf4j.LoggerFactory;

/**
 * Humble object interface for logging. This class was created in order to take away the 3rd party
 * dependency of the logger. The idea is to have the logger dependency only in this class. If there
 * ever is a reason to use a different logger, then we only need to make the changes here.
 */
public class LocalLogger implements Logger {

    private static final LocalLogger INSTANCE = new LocalLogger();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("local");

    // ANSI escape code
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private LocalLogger() {}

    public static LocalLogger getLogger() {
        return INSTANCE;
    }

    @Override
    public void logTrace(String traceMessage) {
        Slf4jLogger.getLoggingEventBuilder(Level.TRACE, ANSI_PURPLE + traceMessage + ANSI_RESET)
                .log();
    }

    @Override
    public void logDebug(String debugMessage) {
        Slf4jLogger.getLoggingEventBuilder(Level.DEBUG, ANSI_CYAN + debugMessage + ANSI_RESET)
                .log();
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        Slf4jLogger.getLoggingEventBuilder(Level.DEBUG, ANSI_CYAN + debugMessage + ANSI_RESET)
                .setCause(e)
                .log();
    }

    @Override
    public void logInfo(String infoMessage, Object... parameters) {
        var logBuilder =
                Slf4jLogger.getLoggingEventBuilder(
                        Level.INFO, ANSI_GREEN + infoMessage + ANSI_RESET);

        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);

        logBuilder.log();
    }

    @Override
    public void logWarning(String warningMessage) {
        Slf4jLogger.getLoggingEventBuilder(Level.WARN, ANSI_YELLOW + warningMessage + ANSI_RESET)
                .log();
    }

    @Override
    public void logError(String errorMessage) {
        Slf4jLogger.getLoggingEventBuilder(Level.ERROR, ANSI_RED + errorMessage + ANSI_RESET).log();
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        Slf4jLogger.getLoggingEventBuilder(Level.ERROR, ANSI_RED + errorMessage + ANSI_RESET)
                .setCause(e)
                .log();
    }

    @Override
    public void logFatal(String fatalMessage) {
        Slf4jLogger.getLoggingEventBuilder(Level.FATAL, ANSI_RED + fatalMessage + ANSI_RESET).log();
    }

    @Override
    public void logFatal(String fatalMessage, Throwable e) {
        Slf4jLogger.getLoggingEventBuilder(Level.FATAL, ANSI_RED + fatalMessage + ANSI_RESET)
                .setCause(e)
                .log();
    }
}
