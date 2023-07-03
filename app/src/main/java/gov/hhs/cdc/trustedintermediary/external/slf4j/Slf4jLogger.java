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
public class Slf4jLogger implements Logger {

    private static final Slf4jLogger INSTANCE = new Slf4jLogger();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("tilogger");

    private Slf4jLogger() {}

    public static Slf4jLogger getLogger() {
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
            case TRACE -> LOGGER.atTrace().setMessage(() -> message);
            case DEBUG -> LOGGER.atDebug().setMessage(() -> message);
            case INFO -> LOGGER.atInfo().setMessage(() -> message);
            case WARN -> LOGGER.atWarn().setMessage(() -> message);
            case ERROR -> LOGGER.atError().setMessage(() -> message);
            case FATAL -> {
                Marker fatal = MarkerFactory.getMarker("FATAL");
                yield LOGGER.atError().addMarker(fatal).setMessage(() -> message);
            }
        };
    }
}
