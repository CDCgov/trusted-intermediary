package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException
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
        1 * authController.constructResponse(400)
    }

    def "handleAuth creates a 401 response when the token is invalid"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        def authController = Mock(AuthController)
        def authUsecase = Mock(RequestSessionTokenUsecase)
        authUsecase.getToken(_) >> { throw new InvalidTokenException(new NullPointerException()) }
        TestApplicationContext.register(AuthController, authController)
        TestApplicationContext.register(RequestSessionTokenUsecase, authUsecase)
        TestApplicationContext.register(AuthDomainRegistration, domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleAuth(new DomainRequest())

        then:
        1 * authController.constructResponse(401)
    }

    def "handleAuth creates a 401 response when the organization is unknown"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        def authController = Mock(AuthController)
        def authUsecase = Mock(RequestSessionTokenUsecase)
        authUsecase.getToken(_) >> { throw new UnknownOrganizationException("a message") }
        TestApplicationContext.register(AuthController, authController)
        TestApplicationContext.register(RequestSessionTokenUsecase, authUsecase)
        TestApplicationContext.register(AuthDomainRegistration, domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleAuth(new DomainRequest())

        then:
        1 * authController.constructResponse(401)
    }

    def "handleAuth creates a 500 response when the usecase blows up"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        def authController = Mock(AuthController)
        def authUsecase = Mock(RequestSessionTokenUsecase)
        authUsecase.getToken(_) >> { throw new NullPointerException() }
        TestApplicationContext.register(AuthController, authController)
        TestApplicationContext.register(RequestSessionTokenUsecase, authUsecase)
        TestApplicationContext.register(AuthDomainRegistration, domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleAuth(new DomainRequest())

        then:
        1 * authController.constructResponse(500)
    }

    def "handleAuth creates a 500 response when the controller can't construct a response"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        def authController = Mock(AuthController)
        def authUsecase = Mock(RequestSessionTokenUsecase)
        authController.constructPayload(_, _) >> { throw new NullPointerException() }
        TestApplicationContext.register(AuthController, authController)
        TestApplicationContext.register(RequestSessionTokenUsecase, authUsecase)
        TestApplicationContext.register(AuthDomainRegistration, domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleAuth(new DomainRequest())

        then:
        1 * authController.constructResponse(500)
    }

    def "handleAuth creates a 200 response when the controller can't construct a response"() {
        given:
        def domainRegistration = new AuthDomainRegistration()

        def authController = Mock(AuthController)
        def authUsecase = Mock(RequestSessionTokenUsecase)
        TestApplicationContext.register(AuthController, authController)
        TestApplicationContext.register(RequestSessionTokenUsecase, authUsecase)
        TestApplicationContext.register(AuthDomainRegistration, domainRegistration)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        domainRegistration.handleAuth(new DomainRequest())

        then:
        1 * authController.constructResponse(200, _)
    }
}
