package gov.hhs.cdc.trustedintermediary.etor.results

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import gov.hhs.cdc.trustedintermediary.ResultMock
import spock.lang.Specification

class ResultResponseTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(ResultResponse.class)

        then:
        noExceptionThrown()
    }

    def "test results constructor"() {
        given:
        def expectedResourceId = "12345678"

        def result = new ResultMock(expectedResourceId, null, null, null, null, null, null)

        when:
        def actual = new ResultResponse(result)

        then:
        actual.getFhirResourceId() == expectedResourceId
    }

    def "test argument constructor"() {
        given:
        def expectedResourceId = "12345678"

        when:
        def actual = new ResultResponse(expectedResourceId)

        then:
        actual.getFhirResourceId() == expectedResourceId
    }
}
