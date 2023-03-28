package gov.hhs.cdc.trustedintermediary.external.azure

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext

import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

class AzureSecretsTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Secrets, AzureSecrets.getInstance())
    }

    def "getSecret works"() {
        given:
        def mockAzureKeyVault = Mock(KeyVault)
        ApplicationContext.register(KeyVault, mockAzureKeyVault)
        ApplicationContext.injectRegisteredImplementations()
        mockAzureKeyVault.getKey(_ as String) >> "mock sender key"
        def expected = "mock sender key"

        when:
        def actual = AzureSecrets.getInstance().getKey()

        then:
        actual == expected
        1 * mockAzureKeyVault.getKey(_ as String) >> "${expected}"
        noExceptionThrown()
    }
}
