package gov.hhs.cdc.trustedintermediary.auth

import spock.lang.Specification

class AuthRequestValidatorTest extends Specification{

    def "tokenHascontent returns false when empty"() {
        given:
        def emptyToken = ""
        def expected = false
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(emptyToken)
        then:actual == expected
    }

    def "tokenHascontent happy path returns true"() {
        given:
        def Token = "I'm not empty"
        def expected = true
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(Token)
        then:actual == expected
    }
}
