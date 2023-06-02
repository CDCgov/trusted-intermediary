package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import spock.lang.Specification

class AuthDomainRegistrationTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def "domain registration has endpoints"() {
        given:
        def domainRegistration = new AuthDomainRegistration()
        def specifiedEndpoint = new HttpEndpoint("POST", "/v1/auth")

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(specifiedEndpoint) != null
    }

    def "has an OpenAPI specification"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        when:
        def openApiSpecification = domainRegistration.openApiSpecification()

        then:
        !openApiSpecification.isEmpty()
        openApiSpecification.contains("paths:")
    }

    def "handleAuth creates a 400 response when can't parse request"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        def authController = Mock(AuthController)
        authController.parseAuthRequest(_ as DomainRequest) >> { throw new IllegalArgumentException() }
        TestApplicationContext.register(AuthController, authController)
        TestApplicationContext.register(AuthDomainRegistration, domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleAuth(new DomainRequest())

        then:
        1 * authController.constructResponse(_ as Integer) >> { Integer statusCode ->
            assert statusCode == 400
        }
    }
}
