package gov.hhs.cdc.trustedintermediary.wrappers;

/** Interface that will be implemented with a humble object for logging */
public interface MyLogger {

    void logInfo(String infoMessage);

    void logWarning(String warningMessage);

    void logDebug(String debugMessage);

    void logError(String errorMessage);

    void logTrace(String traceMessage);

    void logDebug(String debugMessage, Throwable e);

    void logError(String errorMessage, Throwable e);
}
