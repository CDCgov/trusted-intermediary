package gov.hhs.cdc.trustedintermediary.auth

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
    }

    def "organization is not found"() {
    }

    def "organization is lacking a public key"() {
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
