package gov.hhs.cdc.trustedintermediary.context

import io.github.cdimascio.dotenv.Dotenv
import spock.lang.Specification


class DotEnvTest extends Specification {

    def "get method should return value for existing key"() {
        given:
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get("test_id") >> "test_value"
        DotEnv.load(mockDotenv)

        when:
        String value = DotEnv.get("test_id")

        then:
        value == "test_value"
    }

    def "get method should return null for non-existent key"() {
        given:
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get("nonexistent_key") >> null
        DotEnv.load(mockDotenv)

        when:
        String value = DotEnv.get("nonexistent_key")

        then:
        value == null
    }

    def "get method with default value should return value for existing key"() {
        given:
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get("test_key", "default_value") >> "test_value"
        DotEnv.load(mockDotenv)

        when:
        String value = DotEnv.get("test_key", "default_value")

        then:
        value == "test_value"
    }

    def "get method with default value should return default value for non-existent key"() {
        given:
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get("nonexistent_key", "default_value") >> "default_value"
        DotEnv.load(mockDotenv)

        when:
        String value = DotEnv.get("nonexistent_key", "default_value")

        then:
        value == "default_value"
    }
}
