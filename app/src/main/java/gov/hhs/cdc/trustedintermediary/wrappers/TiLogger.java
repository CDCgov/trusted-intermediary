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

    @Override
    public void logInfo(String infoMessage) {
        LOGGER.info(infoMessage);
    }

    @Override
    public void logWarning(String warningMessage) {
        LOGGER.warn(warningMessage);
    }

    @Override
    public void logDebug(String debugMessage) {
        LOGGER.debug(debugMessage);
    }

    @Override
    public void logError(String errorMessage) {
        LOGGER.error(errorMessage);
    }

    @Override
    public void logTrace(String traceMessage) {
        LOGGER.trace(traceMessage);
    }

    @Override
    public void logInfo(String infoMessage, Throwable e) {
        LOGGER.info(infoMessage, e);
    }

    @Override
    public void logWarning(String warningMessage, Throwable e) {
        LOGGER.warn(warningMessage, e);
    }

    @Override
    public void logDebug(String debugMessage, Throwable e) {
        LOGGER.debug(debugMessage, e);
    }

    @Override
    public void logError(String errorMessage, Throwable e) {
        LOGGER.error(errorMessage, e);
    }

    @Override
    public void logTrace(String traceMessage, Throwable e) {
        LOGGER.trace(traceMessage, e);
    }
}
