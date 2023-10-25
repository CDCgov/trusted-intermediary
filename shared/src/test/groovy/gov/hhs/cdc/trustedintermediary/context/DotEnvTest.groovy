package gov.hhs.cdc.trustedintermediary.context

import io.github.cdimascio.dotenv.Dotenv
import spock.lang.Specification


class DotEnvTest extends Specification {

    def "get method should return value for existing key"() {
        given:
        def expected = "test_value"
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get("test_id") >> expected
        DotEnv.load(mockDotenv)

        when:
        def actual = DotEnv.get("test_id")

        then:
        actual == expected
    }

    def "get method should return null for non-existent key"() {
        given:
        def expected = null
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get("nonexistent_key") >> expected
        DotEnv.load(mockDotenv)

        when:
        def actual = DotEnv.get("nonexistent_key")

        then:
        actual == expected
    }

    def "get method with default value should return value for existing key"() {
        given:
        def expected = "test_value"
        def key = "test_key"
        def defaultValue = "default_value"
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get(key, defaultValue) >> expected
        DotEnv.load(mockDotenv)

        when:
        def actual = DotEnv.get(key, defaultValue)

        then:
        actual == expected
    }

    def "get method with default value should return default value for non-existent key"() {
        given:
        def expected = "default_value"
        def key = "nonexistent_key"
        def mockDotenv = Mock(Dotenv)
        mockDotenv.get(key, expected) >> expected
        DotEnv.load(mockDotenv)

        when:
        def actual = DotEnv.get(key, expected)

        then:
        actual == expected
    }
}
