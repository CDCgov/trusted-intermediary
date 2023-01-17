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
        def id = "67890asdfg"

        def demographics = new PatientDemographics()
        demographics.setRequestId(id)

        when:
        def response = new PatientDemographicsResponse(demographics)

        then:
        response.getId() == id
    }

    def "test argument constructor"() {
        given:
        def id = "67890asdfg"

        when:
        def response = new PatientDemographicsResponse(id)

        then:
        response.getId() == id
    }
}
