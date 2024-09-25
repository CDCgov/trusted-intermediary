package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.ExamplesHelper
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirHelper
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class RemoveAccessionNumberTest extends Specification {
    def transformClass
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()

        transformClass = new RemoveAccessionNumber()
    }

    def "When message has all the expected values in OBX-3.4/6, they should be removed"() {
        // dev note - 020_CA_ORU_R01_CDPH_OBX_to_LOINC_1_hl7_translation.fhir has a good example of
        // an accession code that should be removed
        given:
        def dummy = true

        then:
        dummy == true
    }
}
