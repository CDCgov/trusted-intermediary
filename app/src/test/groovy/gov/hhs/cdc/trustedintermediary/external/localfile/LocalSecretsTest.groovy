package gov.hhs.cdc.trustedintermediary.external.localfile

import spock.lang.Specification

class LocalSecretsTest extends Specification {

    def "getKey works"() {
        given:
        when:
        def secrets = LocalSecrets.getInstance().getKey()
        then:
        noExceptionThrown()
    }
}
