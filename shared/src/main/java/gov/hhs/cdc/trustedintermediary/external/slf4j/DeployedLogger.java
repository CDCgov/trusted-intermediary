package gov.hhs.cdc.trustedintermediary.external.slf4j;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Humble object interface for logging. Uses SLF4J behind the scenes. The deployed logger doesn't
 * colorize its messages and uses a logger name that uses the JSON structured logging.
 */
public class DeployedLogger implements Logger {

    private static final DeployedLogger INSTANCE = new DeployedLogger();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("tilogger");

    private DeployedLogger() {}

    public static DeployedLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public void logDebug(String debugMessage) {
        LoggerHelper.logMessageAtLevel(LOGGER, Level.DEBUG, debugMessage).log();
    }

    @Override
    public void logInfo(String infoMessage, Object... parameters) {
        var logBuilder = LoggerHelper.logMessageAtLevel(LOGGER, Level.INFO, infoMessage);

        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);

        logBuilder.log();
    }

    @Override
    public void logMap(String baseMessage, Map<String, Object> map) {
        Level level = Level.INFO;
        var logBuilder = LoggerHelper.logMessageAtLevel(LOGGER, level, baseMessage);

        map.forEach(logBuilder::addKeyValue);
        logBuilder.log();
    }

    @Override
    public void logWarning(String warningMessage, Object... parameters) {
        var logBuilder = LoggerHelper.logMessageAtLevel(LOGGER, Level.WARN, warningMessage);
        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);
        logBuilder.log();
    }

    @Override
    public void logError(String errorMessage) {
        LoggerHelper.logMessageAtLevel(LOGGER, Level.ERROR, errorMessage).log();
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        LoggerHelper.logMessageAtLevel(LOGGER, Level.ERROR, errorMessage).setCause(e).log();
    }

    @Override
    public void logFatal(String fatalMessage, Throwable e) {
        LoggerHelper.addFatalMarker(
                        LoggerHelper.logMessageAtLevel(LOGGER, Level.ERROR, fatalMessage))
                .setCause(e)
                .log();
    }
}
