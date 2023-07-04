package gov.hhs.cdc.trustedintermediary.external.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

class LoggerHelper {
    public static LoggingEventBuilder logMessageAtLevel(
            Logger logger, Level level, String message) {
        return logger.atLevel(level).setMessage(() -> message);
    }

    public static LoggingEventBuilder addFatalMarker(LoggingEventBuilder loggingBuilder) {
        Marker fatal = MarkerFactory.getMarker("FATAL");
        return loggingBuilder.addMarker(fatal);
    }
}
