package gov.hhs.cdc.trustedintermediary.external.apache

import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import org.apache.hc.core5.http.message.BasicHeader
import spock.lang.Specification

class ApacheClientTest extends Specification {

    def "Http request with error"() {
        given:
        def httpClient = ApacheClient.getInstance()

        when:
        def res = httpClient.post("https://fake-uri.com", null, "fake body")

        then:
        thrown(HttpClientException)
    }
}
