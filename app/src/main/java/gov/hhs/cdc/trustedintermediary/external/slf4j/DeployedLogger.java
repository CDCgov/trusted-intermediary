package gov.hhs.cdc.trustedintermediary.external.slf4j;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Arrays;
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
    public void logTrace(String traceMessage) {
        Level level = Level.TRACE;
        LoggerHelper.logMessageAtLevel(LOGGER, level, traceMessage).log();
    }

    @Override
    public void logDebug(String debugMessage) {
        Level level = Level.DEBUG;
        LoggerHelper.logMessageAtLevel(LOGGER, level, debugMessage).log();
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        Level level = Level.DEBUG;
        LoggerHelper.logMessageAtLevel(LOGGER, level, debugMessage).setCause(e).log();
    }

    @Override
    public void logInfo(String infoMessage, Object... parameters) {
        Level level = Level.INFO;
        var logBuilder = LoggerHelper.logMessageAtLevel(LOGGER, level, infoMessage);

        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);

        logBuilder.log();
    }

    @Override
    public void logWarning(String warningMessage) {
        Level level = Level.WARN;
        LoggerHelper.logMessageAtLevel(LOGGER, level, warningMessage).log();
    }

    @Override
    public void logError(String errorMessage) {
        Level level = Level.ERROR;
        LoggerHelper.logMessageAtLevel(LOGGER, level, errorMessage).log();
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        Level level = Level.ERROR;
        LoggerHelper.logMessageAtLevel(LOGGER, level, errorMessage).setCause(e).log();
    }

    @Override
    public void logFatal(String fatalMessage) {
        Level level = Level.ERROR;
        LoggerHelper.addFatalMarker(LoggerHelper.logMessageAtLevel(LOGGER, level, fatalMessage))
                .log();
    }

    @Override
    public void logFatal(String fatalMessage, Throwable e) {
        Level level = Level.ERROR;
        LoggerHelper.addFatalMarker(LoggerHelper.logMessageAtLevel(LOGGER, level, fatalMessage))
                .setCause(e)
                .log();
    }
}
