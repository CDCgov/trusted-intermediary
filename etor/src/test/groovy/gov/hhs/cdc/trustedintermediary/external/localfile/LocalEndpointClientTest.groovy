package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClientException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class LocalEndpointClientTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def cleanup() {
        Files.deleteIfExists(Paths.get(LocalEndpointClient.LOCAL_FILE_NAME))
    }

    def "getRsToken happy path"() {
        when:
        def token = LocalEndpointClient.getInstance().getRsToken()

        then:
        token != null
    }

    def "requestWatersEndpoint happy path"() {
        given:
        def testStringOrder = "Some String"

        when:
        def token = LocalEndpointClient.getInstance().getRsToken()
        LocalEndpointClient.getInstance().requestWatersEndpoint(testStringOrder, token)

        then:
        Files.readString(Paths.get(LocalEndpointClient.LOCAL_FILE_NAME)) == testStringOrder
    }

    def "requestHistoryEndpoint happy path"() {
        when:
        def token = LocalEndpointClient.getInstance().getRsToken()
        def response = LocalEndpointClient.getInstance().requestHistoryEndpoint("order", token)
        def responseObject =
                Jackson.getInstance().convertJsonToObject(response, new TypeReference<Map<String, Object>>() {})
        def destination = responseObject.get("destinations").get(0)

        then:
        destination.get("organization_id") != null
        destination.get("service") != null
    }

    def "requestWatersEndpoint throws an exception when not able to save file with order"() {
        given:
        LocalEndpointClient.LOCAL_FILE_NAME = "some/path/that/does/not/exist"

        when:
        def token = LocalEndpointClient.getInstance().getRsToken()
        LocalEndpointClient.getInstance().requestWatersEndpoint("order", token)

        then:
        thrown(ReportStreamEndpointClientException)
    }
}
