package gov.hhs.cdc.trustedintermediary.etor

import spock.lang.Specification

class keyCacheTest extends Specification {

    def "keyCache works"() {
        given:
        def cache = KeyCache.getInstance()
        def expected = "fake_key"
        def key = "report_stream"
        when:
        cache.put(key, expected)
        def actual = cache.get(key)
        then:
        actual == expected
    }
}
