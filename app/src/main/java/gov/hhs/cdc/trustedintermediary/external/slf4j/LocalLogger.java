package gov.hhs.cdc.trustedintermediary.external.slf4j;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Humble object interface for logging. Uses SLF4J behind the scenes. The local logger colorize its
 * messages and prints things in an easy to read format.
 */
public class LocalLogger implements Logger {

    private static final LocalLogger INSTANCE = new LocalLogger();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("local");

    // ANSI escape code
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private static final Map<Level, String> LEVEL_COLOR_MAPPING =
            Map.of(
                    Level.TRACE,
                    ANSI_PURPLE,
                    Level.DEBUG,
                    ANSI_CYAN,
                    Level.INFO,
                    ANSI_GREEN,
                    Level.WARN,
                    ANSI_YELLOW,
                    Level.ERROR,
                    ANSI_RED);

    private LocalLogger() {}

    public static LocalLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public void logTrace(String traceMessage) {
        Level level = Level.TRACE;
        LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), traceMessage))
                .log();
    }

    @Override
    public void logDebug(String debugMessage) {
        Level level = Level.DEBUG;
        LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), debugMessage))
                .log();
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        Level level = Level.DEBUG;
        LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), debugMessage))
                .setCause(e)
                .log();
    }

    @Override
    public void logInfo(String infoMessage, Object... parameters) {
        Level level = Level.INFO;
        var logBuilder =
                LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), infoMessage));

        Arrays.stream(parameters).forEachOrdered(logBuilder::addArgument);

        logBuilder.log();
    }

    @Override
    public void logWarning(String warningMessage) {
        Level level = Level.WARN;
        LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), warningMessage))
                .log();
    }

    @Override
    public void logError(String errorMessage) {
        Level level = Level.ERROR;
        LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), errorMessage))
                .log();
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        Level level = Level.ERROR;
        LoggerHelper.logMessageAtLevel(
                        LOGGER,
                        level,
                        wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), errorMessage))
                .setCause(e)
                .log();
    }

    @Override
    public void logFatal(String fatalMessage) {
        Level level = Level.ERROR;
        LoggerHelper.addFatalMarker(
                        LoggerHelper.logMessageAtLevel(
                                LOGGER,
                                level,
                                wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), fatalMessage)))
                .log();
    }

    @Override
    public void logFatal(String fatalMessage, Throwable e) {
        Level level = Level.ERROR;
        LoggerHelper.addFatalMarker(
                        LoggerHelper.logMessageAtLevel(
                                LOGGER,
                                level,
                                wrapMessageInColor(LEVEL_COLOR_MAPPING.get(level), fatalMessage)))
                .setCause(e)
                .log();
    }

    private static String wrapMessageInColor(String color, String message) {
        return color + message + ANSI_RESET;
    }
}
