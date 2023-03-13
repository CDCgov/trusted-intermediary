package gov.hhs.cdc.trustedintermediary.external.apache

import org.apache.hc.core5.http.Header
import org.apache.hc.core5.http.message.BasicHeader
import spock.lang.Specification

class ApacheClientTest extends Specification {

    def "convertMapToHeader works"() {
        given:
        def headerMap = [
            "key": "value",
            "name": "dogCow",
            "first": "last"
        ]
        def bhArr = [
            new BasicHeader("key", "value"),
            new BasicHeader("name", "dogCow"),
            new BasicHeader("first", "last")
        ]

        when:
        def actual = ApacheClient.getInstance().convertMapToHeader(headerMap)

        then:
        actual.toString() == bhArr.toString()
    }
}
