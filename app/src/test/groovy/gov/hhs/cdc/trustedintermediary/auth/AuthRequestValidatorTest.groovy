package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import spock.lang.Specification

class AuthRequestValidatorTest extends Specification{

    def "tokenHascontent unhappy path works"() {
        given:
        def emptyToken = ""
        def expected = false
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(emptyToken)

        then:
        actual == expected
    }

    def "tokenHascontent happy path works"() {
        given:
        def Token = "I'm not empty"
        def expected = true
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(Token)

        then:
        actual == expected
    }

    def "extractToken happy path works"() {
        given:
        def token = "fake-token-here"
        def header = Map.of("Authorization", "Bearer " + token)
        def expected = token
        def request = new DomainRequest()
        def validator = AuthRequestValidator.getInstance()

        when:
        request.setHeaders(header)
        def actual = validator.extractToken(request)

        then:
        actual == expected
    }

    def "extractToken unhappy path works"() {
        given:
        def header = Map.of("key", "value")
        def expected = ""
        def request = new DomainRequest()
        def validator = AuthRequestValidator.getInstance()

        when:
        request.setHeaders(header)
        def actual = validator.extractToken(request)

        then:
        actual == expected
    }
}
