package gov.hhs.cdc.trustedintermediary.context

import io.github.cdimascio.dotenv.Dotenv
import spock.lang.Specification

import java.lang.reflect.Field

class DotEnvTest extends Specification {
    def "get method should return value for existing key"() {
        given:
        def dotenv = Mock(Dotenv)
        Object st = DotEnv.getMetaClass().getStatic()
        //        dotenv.get("test_key") >> "test_value"
        //        final var dotenv = Dotenv.configure().directory("app/src/main/resources").ignoreIfMissing().load();
        //        DotEnv.mixin()

        //        Dotenv dotenv = GroovyMock(global: true) as Dotenv
        //        dotenv.get("test_key" as String) >> "test_value"


        //        Field field = DotEnv.class.getDeclaredField("DOTENV")
        //        field.setAccessible(true)
        //        field.set(null, dotenv)

        //        DotEnv.mixin(new Expando() {
        //            def get(key) {
        //                return dotenv.get(key)
        //            }
        //        } as Class)

        when:
        String value = DotEnv.get("test_key")

        then:
        value == "test_value"
    }

    def "get method should return null for non-existent key"() {
        given:
        def dotenv = Mock(Dotenv)
        dotenv.get("nonexistent_key") >> null

        when:
        String value = DotEnv.get("nonexistent_key")

        then:
        value == null
    }

    def "get method with default value should return value for existing key"() {
        given:
        Dotenv dotenv = Mock(Dotenv)
        dotenv.get("test_key", "default_value") >> "test_value"

        when:
        String value = DotEnv.get("test_key", "default_value")

        then:
        value == "test_value"
    }

    def "get method with default value should return default value for non-existent key"() {
        given:
        Dotenv dotenv = Mock(Dotenv)
        dotenv.get("nonexistent_key", "default_value") >> "default_value"

        when:
        String value = DotEnv.get("nonexistent_key", "default_value")

        then:
        value == "default_value"
    }
}
