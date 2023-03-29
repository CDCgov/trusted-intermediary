package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class HapiDemographicsTest extends Specification {

    def "getUnderlyingDemographics works"() {
        given:
        def expectedInnerDemographics = new Bundle()
        def demographics = new HapiDemographics(expectedInnerDemographics)

        when:
        def actualInnerDemographics = demographics.getUnderlyingDemographics()

        then:
        actualInnerDemographics == expectedInnerDemographics
    }

    def "getFhirResourceId works"() {
        given:
        def expectedId = "DogCow goes Moof"
        def innerDemographics = new Bundle()
        innerDemographics.setId(expectedId)

        when:
        def demographics = new HapiDemographics(innerDemographics)

        then:
        demographics.getFhirResourceId() == expectedId
    }

    def "getPatientId works"() {
        given:
        def expectedPatientId = "DogCow goes Moof"
        def innerDemographics = new Bundle()
        def patient = new Patient().addIdentifier(new Identifier()
                .setValue(expectedPatientId)
                .setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "MR", "Medical Record Number"))))
        innerDemographics.addEntry(new Bundle.BundleEntryComponent().setResource(patient))

        when:
        def demographics = new HapiDemographics(innerDemographics)

        then:
        demographics.getPatientId() == expectedPatientId
    }
}
