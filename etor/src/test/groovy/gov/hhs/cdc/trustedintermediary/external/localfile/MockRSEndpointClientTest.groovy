package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MockRSEndpointClientTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def cleanup() {
        Files.deleteIfExists(Paths.get(MockRSEndpointClient.LOCAL_FILE_NAME))
    }

    def "getRsToken happy path"() {
        when:
        def token = MockRSEndpointClient.getInstance().getRsToken()

        then:
        token != null
    }

    def "requestWatersEndpoint happy path"() {
        given:
        def testStringOrder = "Some String"

        when:
        MockRSEndpointClient.getInstance().requestWatersEndpoint(testStringOrder, "token")

        then:
        Files.readString(Paths.get(MockRSEndpointClient.LOCAL_FILE_NAME)) == testStringOrder
    }

    def "requestHistoryEndpoint happy path"() {
        when:
        def token = MockRSEndpointClient.getInstance().getRsToken()
        def response = MockRSEndpointClient.getInstance().requestHistoryEndpoint("order", token)
        def responseObject =
                Jackson.getInstance().convertJsonToObject(response, new TypeReference<Map<String, Object>>() {})
        def destination = responseObject.get("destinations").get(0)

        then:
        destination.get("organization_id") != null
        destination.get("service") != null
    }

    def "requestWatersEndpoint throws an exception when not able to save file with order"() {
        given:
        Path readonlyLocalFile = Paths.get(MockRSEndpointClient.LOCAL_FILE_NAME)
        Files.createFile(readonlyLocalFile)
        readonlyLocalFile.toFile().setReadOnly()

        when:
        MockRSEndpointClient.getInstance().requestWatersEndpoint("order", "token")

        then:
        thrown(ReportStreamEndpointClientException)

        cleanup:
        readonlyLocalFile.toFile().delete()
    }
}
