package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Organization
import spock.lang.Specification

class UpdateReceivingApplicationNamespaceTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(MetricMetadata, Mock(MetricMetadata))
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new UpdateReceivingApplicationNamespace()
    }

    def "update receiving application namespace to given name"() {
        given:
        def name = "EPIC"
        def bundle = FhirBundleHelper.createMessageBundle(messageTypeCode: 'ORM_O01')

        expect:
        HapiHelper.resourceInBundle(bundle, Organization).isEmpty()

        when:
        transformClass.transform(new HapiFhirResource(bundle), Map.of("name", name))
        def header = HapiHelper.getMessageHeader(bundle)
        def org = header.destination.first().getReceiver().getResource() as Organization

        then:
        org.name == name
    }
}
