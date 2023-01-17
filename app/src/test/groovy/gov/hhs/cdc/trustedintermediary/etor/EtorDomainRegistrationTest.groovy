package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.etor.order.PatientDemographics
import gov.hhs.cdc.trustedintermediary.etor.order.OrderController
import gov.hhs.cdc.trustedintermediary.etor.order.OrderMessage
import spock.lang.Specification

class EtorDomainRegistrationTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def "domain registration has endpoints"() {
        given:
        def domainRegistration = new EtorDomainRegistration()
        def specifiedEndpoint = new HttpEndpoint("POST", "/v1/etor/order")

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(specifiedEndpoint) != null
    }

    def "has an OpenAPI specification"() {
        given:
        def domainRegistration = new EtorDomainRegistration()

        when:
        def openApiSpecification = domainRegistration.openApiSpecification()

        then:
        noExceptionThrown()
        !openApiSpecification.isEmpty()
        openApiSpecification.contains("paths:")
    }

    def "stitches the order parsing to the response construction"() {
        given:
        def domainRegistration = new EtorDomainRegistration()

        def mockOrderController = Mock(OrderController)

        def mockOrderId = "asdf-12341-jkl-7890"

        mockOrderController.parseOrder(_ as DomainRequest) >> new PatientDemographics(mockOrderId, "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow")
        mockOrderController.constructResponse(_ as OrderMessage) >> new DomainResponse(418)

        def domainRequest = new DomainRequest()

        TestApplicationContext.register(EtorDomainRegistration, domainRegistration)
        TestApplicationContext.register(OrderController, mockOrderController)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = domainRegistration.handleOrder(domainRequest)

        then:
        1 * mockOrderController.constructResponse(_ as OrderMessage) >> { OrderMessage orderMessage ->
            assert orderMessage.id == mockOrderId
        }
    }
}
