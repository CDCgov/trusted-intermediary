package gov.hhs.cdc.trustedintermediary.external.slf4j;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LoggingEventBuilder;

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
        getLoggingEventBuilder(Level.TRACE, traceMessage).log();
    }

    @Override
    public void logDebug(String debugMessage) {
        getLoggingEventBuilder(Level.DEBUG, debugMessage).log();
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        getLoggingEventBuilder(Level.DEBUG, debugMessage).setCause(e).log();
    }

    @Override
    public void logInfo(String infoMessage, Object... parameters) {
        var logBuilder = getLoggingEventBuilder(Level.INFO, infoMessage);

        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);

        logBuilder.log();
    }

    @Override
    public void logWarning(String warningMessage) {
        getLoggingEventBuilder(Level.WARN, warningMessage).log();
    }

    @Override
    public void logError(String errorMessage) {
        getLoggingEventBuilder(Level.ERROR, errorMessage).log();
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        getLoggingEventBuilder(Level.ERROR, errorMessage).setCause(e).log();
    }

    @Override
    public void logFatal(String fatalMessage) {
        getLoggingEventBuilder(Level.FATAL, fatalMessage).log();
    }

    @Override
    public void logFatal(String fatalMessage, Throwable e) {
        getLoggingEventBuilder(Level.FATAL, fatalMessage).setCause(e).log();
    }

    protected static LoggingEventBuilder getLoggingEventBuilder(Level level, String message) {
        return switch (level) {
            case TRACE -> LOGGER.atTrace().setMessage(() -> ANSI_PURPLE + message + ANSI_RESET);
            case DEBUG -> LOGGER.atDebug().setMessage(() -> ANSI_CYAN + message + ANSI_RESET);
            case INFO -> LOGGER.atInfo().setMessage(() -> ANSI_GREEN + message + ANSI_RESET);
            case WARN -> LOGGER.atWarn().setMessage(() -> ANSI_YELLOW + message + ANSI_RESET);
            case ERROR -> LOGGER.atError().setMessage(() -> ANSI_RED + message + ANSI_RESET);
            case FATAL -> {
                Marker fatal = MarkerFactory.getMarker("FATAL");
                yield LOGGER.atError()
                        .addMarker(fatal)
                        .setMessage(() -> ANSI_RED + message + ANSI_RESET);
            }
        };
    }
}
