package gov.hhs.cdc.trustedintermediary.wrappers;

/** Interface that will be implemented with a humble object for logging */
public interface Logger {

    void logTrace(String traceMessage);

    void logDebug(String debugMessage);

    void logDebug(String debugMessage, Throwable e);

    void logInfo(String infoMessage, Object... parameters);

    void logWarning(String warningMessage);

    void logError(String errorMessage);

    void logError(String errorMessage, Throwable e);

    void logFatal(String errorMessage);

    void logFatal(String errorMessage, Throwable e);
}
