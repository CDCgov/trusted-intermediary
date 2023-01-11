package gov.hhs.cdc.trustedintermediary.external.javalin

import gov.hhs.cdc.trustedintermediary.OpenApi
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HandlerType
import spock.lang.Specification

import java.util.function.Function

class DomainsRegistrationTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OpenApi, Mock(OpenApi))
        Example1DomainConnector.endpointCount = 0
        Example2DomainConnector.endpointCount = 0
        OpenApiCalledDomainConnector.openApiSecificationMethodWasCalled = false
    }

    def "convert Javalin Context to DomainRequest correctly"() {
        given:

        def bodyString = "DogCow"
        def urlString = "Moof"
        def headerMap = [
            "Clarus": "is a DogCow"
        ]

        def javalinContext = Mock(Context)
        javalinContext.body() >> bodyString
        javalinContext.url() >> urlString
        javalinContext.headerMap() >> headerMap

        when:
        def domainRequest = DomainsRegistration.javalinContextToDomainRequest(javalinContext)

        then:
        domainRequest.getBody() == bodyString
        domainRequest.getUrl() == urlString
        domainRequest.getHeaders() == headerMap
    }

    def "Inject values from DomainResponse into a Javalin Context correctly"() {
        given:

        def statusCode = 418
        def bodyString = "Moof"
        def headerMap = [
            "Clarus": "DogCow",
            "Two": "Another time"
        ]

        def response = new DomainResponse(statusCode)
        response.setBody(bodyString)
        response.setHeaders(headerMap)

        def javalinContext = Mock(Context)
        def savedResult = null
        def savedStatusCode = null
        def savedHeaders = [:]
        javalinContext.result(_ as String) >> { String result ->
            savedResult = result
            return
        }
        javalinContext.status(_ as Integer) >> { int status ->
            savedStatusCode = status
            return
        }
        javalinContext.header(_ as String, _ as String) >> { String key, String value -> savedHeaders.put(key, value) }

        when:
        DomainsRegistration.domainResponseFillsInJavalinContext(response, javalinContext)

        then:
        savedResult == bodyString
        savedStatusCode == statusCode
        savedHeaders == headerMap
    }

    def "createHandler successfully stitches things together"() {
        given:
        def handlerCalled = false
        def rawHandler = { request ->
            handlerCalled = true
            return new DomainResponse(418)
        }
        def javalinContext = Mock(Context)
        javalinContext.method() >> HandlerType.POST

        when:
        def javalinHandler = DomainsRegistration.createHandler(rawHandler)
        javalinHandler.handle(javalinContext)

        then:
        handlerCalled == true
        1 * javalinContext.status(_ as Integer)
    }

    def "constructNewDomainConnector works correctly with a default constructor"() {
        when:
        def connector = DomainsRegistration.constructNewDomainConnector(GoodDomainConnector)

        then:
        noExceptionThrown()
        connector != null
    }

    def "constructNewDomainConnector fails when there isn't a default constructor"() {
        when:
        DomainsRegistration.constructNewDomainConnector(BadDomainConnector)

        then:
        thrown RuntimeException
    }

    def "every DomainConnector is registered"() {
        given:
        def javalinApp = Mock(Javalin)

        Example1DomainConnector.endpointCount = 2
        Example2DomainConnector.endpointCount = 3
        def expectedNumberOfAddHandlerCalls = Example1DomainConnector.endpointCount + Example2DomainConnector.endpointCount
        def domains = Set.of(Example1DomainConnector, Example2DomainConnector)

        when:
        DomainsRegistration.registerDomains(javalinApp, domains as Set<Class<? extends DomainConnector>>)

        then:
        expectedNumberOfAddHandlerCalls * javalinApp.addHandler(_ as HandlerType, _ as String, _ as Handler)
    }

    def "an OpenAPI endpoint is registered and it sets it content-type as YAML"() {
        given:
        def javalinApp = Mock(Javalin)

        def domains = Set.of(Example1DomainConnector, Example2DomainConnector, OpenApiCalledDomainConnector)

        String contentType = null

        when:
        DomainsRegistration.registerDomains(javalinApp, domains as Set<Class<? extends DomainConnector>>)

        then:
        OpenApiCalledDomainConnector.openApiSecificationMethodWasCalled == true
        javalinApp.get(_ as String, _ as Handler) >> { String path, Handler handler ->
            assert path.contains("openapi")

            def context = Mock(Context)

            context.header(_ as String, _ as String) >> { String key, String value ->
                if (key.equalsIgnoreCase("Content-Type")) {
                    contentType = value
                }
                return context
            }

            handler.handle(context)
        }
        contentType == "application/yaml"
    }

    static class Example1DomainConnector implements DomainConnector {

        static def endpointCount = 0

        @Override
        Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
            Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> registration = new HashMap<>()

            for (int endpointIndex = 0; endpointIndex < endpointCount; endpointIndex ++) {
                Function<DomainRequest, DomainResponse> function = { request -> new DomainResponse(418) }
                registration.put(new HttpEndpoint("PUT", "/dogcow" + endpointIndex), function)
            }

            return registration
        }

        @Override
        String openApiSpecification() {
            return "DogCow"
        }
    }

    static class Example2DomainConnector implements DomainConnector {

        static def endpointCount = 0

        @Override
        Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
            Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> registration = new HashMap<>()

            for (int endpointIndex = 0; endpointIndex < endpointCount; endpointIndex ++) {
                Function<DomainRequest, DomainResponse> function = { request -> new DomainResponse(418) }
                registration.put(new HttpEndpoint("POST", "/moof" + endpointIndex), function)
            }

            return registration
        }

        @Override
        String openApiSpecification() {
            return "DogCow"
        }
    }

    static class GoodDomainConnector implements DomainConnector {
        @Override
        Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
            return null
        }

        @Override
        String openApiSpecification() {
            return "DogCow"
        }
    }

    static class BadDomainConnector implements DomainConnector {
        BadDomainConnector(String differentConstructor) {}

        @Override
        Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
            return null
        }

        @Override
        String openApiSpecification() {
            return "DogCow"
        }
    }

    static class OpenApiCalledDomainConnector implements DomainConnector {

        static def openApiSecificationMethodWasCalled = false

        @Override
        Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
            return Map.of()
        }

        @Override
        String openApiSpecification() {
            openApiSecificationMethodWasCalled = true
            return "DogCow"
        }
    }
}
