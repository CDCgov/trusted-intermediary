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
public class DeployedLogger implements Logger {

    private static final DeployedLogger INSTANCE = new DeployedLogger();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("tilogger");

    private DeployedLogger() {}

    public static DeployedLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public void logTrace(String traceMessage) {
        LOGGER.atTrace().setMessage(() -> traceMessage).log();
    }

    @Override
    public void logDebug(String debugMessage) {
        LOGGER.atDebug().setMessage(() -> debugMessage).log();
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        LOGGER.atDebug().setMessage(() -> debugMessage).setCause(e).log();
    }

    @Override
    public void logInfo(String infoMessage, Object... parameters) {
        var logBuilder = LOGGER.atInfo().setMessage(() -> infoMessage);

        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);

        logBuilder.log();
    }

    @Override
    public void logWarning(String warningMessage) {
        LOGGER.atWarn().setMessage(() -> warningMessage).log();
    }

    @Override
    public void logError(String errorMessage) {
        LOGGER.atError().setMessage(() -> errorMessage).log();
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        LOGGER.atError().setMessage(() -> errorMessage).setCause(e).log();
    }

    @Override
    public void logFatal(String fatalMessage) {
        logAtFatal().setMessage(() -> fatalMessage).log();
    }

    @Override
    public void logFatal(String fatalMessage, Throwable e) {
        logAtFatal().setMessage(() -> fatalMessage).setCause(e).log();
    }

    private LoggingEventBuilder logAtFatal() {
        Marker fatal = MarkerFactory.getMarker("FATAL");
        return LOGGER.atError().addMarker(fatal);
    }
}
