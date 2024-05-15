package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.FhirBundleHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Type
import spock.lang.Specification

class UpdateReceivingApplicationNamespaceTest extends Specification {

    def transformClass

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new UpdateReceivingApplicationNamespace()
    }

    def "update receiving application namespace to given name and remove Universal Id and Universal Id Type "() {
        given:
        def name = "EPIC"
        def bundle = new Bundle()
        HapiHelper.getOrCreateMessageHeader(bundle)
        def receivingApplication = HapiHelper.createReceivingApplication()
        receivingApplication.addExtension(HapiHelper.UNIVERSAL_ID_URL, new StringType("universal-id"))
        receivingApplication.addExtension(HapiHelper.UNIVERSAL_ID_TYPE_URL, new StringType("universal-id-type"))
        HapiHelper.setReceivingApplication(bundle, receivingApplication)

        expect:
        def beforeReceivingApplication = HapiHelper.getReceivingApplication(bundle)
        beforeReceivingApplication.name != name
        beforeReceivingApplication.getExtensionByUrl(HapiHelper.UNIVERSAL_ID_URL) != null
        beforeReceivingApplication.getExtensionByUrl(HapiHelper.UNIVERSAL_ID_TYPE_URL) != null

        when:
        transformClass.transform(new HapiFhirResource(bundle), Map.of("name", name))
        def transformedReceivingApplication = HapiHelper.getReceivingApplication(bundle)

        then:
        transformedReceivingApplication.name == name
        transformedReceivingApplication.getExtensionByUrl(HapiHelper.UNIVERSAL_ID_URL) == null
        transformedReceivingApplication.getExtensionByUrl(HapiHelper.UNIVERSAL_ID_TYPE_URL) == null
    }
}
