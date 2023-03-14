package gov.hhs.cdc.trustedintermediary.external.azure

import spock.lang.Specification

class AzureSecretsTest extends Specification {

    def "getSecret works"() {
        given:
        when:
        def secrets = AzureSecrets.getInstance().getKey()

        then:
        noExceptionThrown()
    }
}
