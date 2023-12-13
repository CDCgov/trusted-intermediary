package gov.hhs.cdc.trustedintermediary.wrappers;

import java.util.Map;

/** Interface that will be implemented with a humble object for logging */
public interface Logger {

    void logDebug(String debugMessage);

    void logInfo(String infoMessage, Object... parameters);

    void logMap(String key, Map<String, Object> map);

    void logWarning(String warningMessage, Object... parameters);

    void logError(String errorMessage);

    void logError(String errorMessage, Throwable e);

    void logFatal(String errorMessage, Throwable e);
}
