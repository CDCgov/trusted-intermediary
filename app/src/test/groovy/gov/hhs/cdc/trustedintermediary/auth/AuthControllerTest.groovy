package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import java.nio.charset.StandardCharsets
import spock.lang.Specification

class AuthControllerTest extends Specification {
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(AuthController, AuthController.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "extractFormUrlEncode grabs everything"() {
        given:
        def scopeString = "scope"
        def scope = "DogCow"
        def soundString = "sound"
        def sound = "Moof!"
        def nameString = "name"
        def name = "Clarus"

        when:
        def extracted = AuthController.getInstance().extractFormUrlEncode(scopeString + "=" + scope +"&" + soundString + "=" + sound + "&" + nameString + "=" + name)

        then:
        extracted.get(scopeString).get() == scope
        extracted.get(soundString).get() == sound
        extracted.get(nameString).get() == name
    }

    def "extractFormUrlEncode gives an empty string with key="() {
        given:
        def scopeString = "scope"

        when:
        def extracted = AuthController.getInstance().extractFormUrlEncode(scopeString + "=")

        then:
        extracted.get(scopeString).get() == ""
    }

    def "extractFormUrlEncode gives an empty value with key (and no equals)"() {
        given:
        def scopeString = "scope"

        when:
        def extracted = AuthController.getInstance().extractFormUrlEncode(scopeString)

        then:
        !extracted.get(scopeString).isPresent()
    }

    def "extractFormUrlEncode gives correctly parses out the value when it has ="() {
        given:
        def scopeString = "scope"
        def scope = "1=2=3"

        when:
        def extracted = AuthController.getInstance().extractFormUrlEncode(scopeString + "=" + scope)

        then:
        extracted.get(scopeString).get() == scope
    }

    def "extractFormUrlEncode gives an empty key with a beginning &"() {
        when:
        def extracted = AuthController.getInstance().extractFormUrlEncode("&dogcow=moof")

        then:
        extracted.get("") != null
        !extracted.get("").isPresent()
    }

    def "extractFormUrlEncode works with a URL-encoded special character"() {
        given:
        def key = "dog%26cow"
        def value = "mo%20of"
        def encodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8)
        def encodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8)

        when:
        def extracted = AuthController.getInstance().extractFormUrlEncode(key + "=" + value)

        then:
        extracted.get(encodedKey).get() == encodedValue
    }
}
