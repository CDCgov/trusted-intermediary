package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamLabOrderSender
import gov.hhs.cdc.trustedintermediary.external.slf4j.Slf4jLogger
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class LocalSecretsTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Secrets.class, LocalSecrets.getInstance())
    }

    def "getKey works"() {
        given:
        TestApplicationContext.register(Logger.class, Slf4jLogger.getLogger())
        TestApplicationContext.injectRegisteredImplementations()

        def expected = new String(Files.readAllBytes(
                Path.of("..", "mock_credentials", "report-stream-sender-private-key-local.pem")
                ))
        when:
        def actual = LocalSecrets.getInstance().getKey("report-stream-sender-private-key-local")

        then:
        noExceptionThrown()
        actual == expected
    }
}
