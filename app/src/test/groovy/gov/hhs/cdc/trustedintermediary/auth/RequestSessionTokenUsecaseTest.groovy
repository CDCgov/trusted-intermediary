package gov.hhs.cdc.trustedintermediary.auth


import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.organizations.Organization
import gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

class RequestSessionTokenUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RequestSessionTokenUsecase, RequestSessionTokenUsecase.getInstance())

        def mockCache = Mock(Cache)
        mockCache.get(_ as String) >> null
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "happy path returns a session token"() {
        given:
        def authEngine = Mock(AuthEngine)
        def secrets = Mock(Secrets)
        def organizationsSettings = Mock(OrganizationsSettings)

        TestApplicationContext.register(AuthEngine, authEngine)
        TestApplicationContext.register(Secrets, secrets)
        TestApplicationContext.register(OrganizationsSettings, organizationsSettings)
        TestApplicationContext.injectRegisteredImplementations()

        def orgOptional = Optional.of(new Organization())
        organizationsSettings.findOrganization(_ as String) >> orgOptional
        secrets.getKey(_ as String) >> "KEY"
        def expectedSessionToken = "SESSION TOKEN"
        authEngine.generateToken(_ as String, _ as String, _ as String, _ as String, 300, _ as String) >> expectedSessionToken

        when:
        def actualSessionToken = RequestSessionTokenUsecase.getInstance().getToken(new AuthRequest("RS", "AUTH TOKEN"))

        then:
        actualSessionToken == expectedSessionToken
    }

    def "organization is not found"() {
        given:
        def organizationsSettings = Mock(OrganizationsSettings)

        TestApplicationContext.register(OrganizationsSettings, organizationsSettings)
        TestApplicationContext.injectRegisteredImplementations()

        organizationsSettings.findOrganization(_ as String) >> Optional.empty()

        when:
        RequestSessionTokenUsecase.getInstance().getToken(new AuthRequest("RS", "AUTH TOKEN"))

        then:
        thrown(UnknownOrganizationException)
    }

    def "organization is lacking a public key"() {
        given:
        def secrets = Mock(Secrets)
        def organizationsSettings = Mock(OrganizationsSettings)

        TestApplicationContext.register(Secrets, secrets)
        TestApplicationContext.register(OrganizationsSettings, organizationsSettings)
        TestApplicationContext.injectRegisteredImplementations()

        organizationsSettings.findOrganization(_ as String) >> Optional.of(new Organization())
        secrets.getKey(_ as String) >> { throw new SecretRetrievalException("", new NullPointerException()) }

        when:
        RequestSessionTokenUsecase.getInstance().getToken(new AuthRequest("RS", "AUTH TOKEN"))

        then:
        thrown(SecretRetrievalException)
    }

    def "client provided credentials are invalid"() {
    }

    def "we've incorrectly configured the organization public key"() {
    }

    def "TI service is lacking a private key"() {
    }

    def "we've incorrectly configured our private key"() {
    }

    def "caching works when retreiving the organization's public key"() {
    }

    def "caching works when retreiving TI's private key"() {
    }
}
