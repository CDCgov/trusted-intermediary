package gov.hhs.cdc.trustedintermediary.external.slf4j

import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext

import org.slf4j.event.Level
import org.slf4j.MarkerFactory

import spock.lang.Specification


class Slf4jLoggerTest extends Specification {

    def "getLoggingEventBuilder returns INFO level builder test"() {
        given:
        TestApplicationContext.injectRegisteredImplementations()
        def logger = Slf4jLogger.getLogger()
        def expectedLevel = Level.INFO
        def expectedMessage = "Info message"

        when:
        def logEventBuilder = logger.getLoggingEventBuilder(Logger.Level.INFO, expectedMessage)
        def actualLevel = logEventBuilder["loggingEvent"]["level"]
        def actualMessage = logEventBuilder["loggingEvent"]["message"]

        then:
        actualLevel == expectedLevel
        actualMessage.toString().contains(expectedMessage.toString())
    }

    def "getLoggingEventBuilder returns ERROR level builder test"() {
        given:
        TestApplicationContext.injectRegisteredImplementations()
        def logger = Slf4jLogger.getLogger()
        def expectedLevel = Level.ERROR
        def expectedMessage = "Trace message"

        when:
        def logEventBuilder = logger.getLoggingEventBuilder(Logger.Level.ERROR, expectedMessage)
        def actualLevel = logEventBuilder["loggingEvent"]["level"]
        def actualMessage = logEventBuilder["loggingEvent"]["message"]

        then:
        actualLevel == expectedLevel
        actualMessage.toString().contains(expectedMessage.toString())
    }

    def "getLoggingEventBuilder returns FATAL level builder test"() {
        given:
        TestApplicationContext.injectRegisteredImplementations()
        def logger = Slf4jLogger.getLogger()
        def fatalLevel = Logger.Level.FATAL
        def expectedLevel = Level.ERROR
        def expectedMessage = "Fatal message"
        def expectedMarker = MarkerFactory.getMarker(fatalLevel.toString())

        when:
        def logEventBuilder = logger.getLoggingEventBuilder(fatalLevel, expectedMessage)
        def actualLevel = logEventBuilder["loggingEvent"]["level"]
        def actualMessage = logEventBuilder["loggingEvent"]["message"]
        def actualMarker = ((ArrayList) logEventBuilder["loggingEvent"]["markers"])[0]

        then:
        actualLevel == expectedLevel
        actualMessage.toString().contains(expectedMessage.toString())
        actualMarker == expectedMarker
    }
}
