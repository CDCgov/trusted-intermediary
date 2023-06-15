package gov.hhs.cdc.trustedintermediary.external.localfile

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import spock.lang.Specification

class LocalSecretsTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Secrets.class, LocalSecrets.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "getKey works"() {
        given:
        def secretName = "trusted-intermediary-private-key-local"  //pragma: allowlist secret
        def expected = Files.readString(Path.of("..", "mock_credentials", secretName + ".pem"))

        when:
        def actual = LocalSecrets.getInstance().getKey(secretName)

        then:
        actual == expected
    }

    def "getKey fails with a non-existing secret"() {
        when:
        LocalSecrets.getInstance().getKey("secret that doesn't exist")

        then:
        thrown(SecretRetrievalException)
    }

    def "readSecretFromFileSystem takes a bad secret name"() {
        given:
        def secrets = LocalSecrets.getInstance()
        def name = "bad secret name"

        when:
        def secret = secrets.readSecretFromFileSystem(name)
        println(secret)

        then:
        def exception = thrown(SecretRetrievalException)
        exception.getCause().getClass() == NoSuchFileException
    }

    def "readSecretFromResources takes a bad secret name"() {
        given:
        def secrets = LocalSecrets.getInstance()
        def name = "bad secret name"

        when:
        def secret = secrets.readSecretFromResources(name)

        then:
        def exception = thrown(Exception)
        exception.getCause().getClass() == NullPointerException
    }

    def "readSecretFromResource works"() {
        given:
        def secretName = "report-stream-sender-private-key-local"  //pragma: allowlist secret
        def expected = Files.readString(Path.of("..", "mock_credentials", secretName + ".pem"))

        when:
        def actual = LocalSecrets.getInstance().readSecretFromResources(secretName)

        then:
        actual == expected
    }
}
