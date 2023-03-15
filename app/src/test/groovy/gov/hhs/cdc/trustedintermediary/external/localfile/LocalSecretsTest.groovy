package gov.hhs.cdc.trustedintermediary.external.localfile

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class LocalSecretsTest extends Specification {

    def "getKey works"() {
        given:
        def expected = new String(Files.readAllBytes(
                Path.of("..", "mock_credentials", "my-rsa-local-private-key.pem")
                ))
        when:
        def actual = LocalSecrets.getInstance().getKey()
        println(actual)

        then:
        actual == expected
        noExceptionThrown()
    }
}
