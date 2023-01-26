package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class PatientDemographicsResponseTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(PatientDemographicsResponse.class)

        then:
        noExceptionThrown()
    }

    def "test demographics constructor"() {
        given:
        def resourceId = "67890asdfg"
        def patientId = "fthgyu687"

        def demographics = new PatientDemographics()
        demographics.setRequestId(resourceId)
        demographics.setPatientId(patientId)

        when:
        def response = new PatientDemographicsResponse(demographics)

        then:
        response.getFhirResourceId() == resourceId
        response.getPatientId() == patientId
    }

    def "test argument constructor"() {
        given:
        def resourceId = "67890asdfg"
        def patientId = "fthgyu687"

        when:
        def response = new PatientDemographicsResponse(resourceId, patientId)

        then:
        response.getFhirResourceId() == resourceId
        response.getPatientId() == patientId
    }
}
