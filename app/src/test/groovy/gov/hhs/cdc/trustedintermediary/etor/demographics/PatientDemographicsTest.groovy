package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

import java.time.ZonedDateTime

class PatientDemographicsTest extends Specification {

    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(PatientDemographics.class)

        then:
        noExceptionThrown()
    }

    def "test default constructor"() {
        when:
        def order = new PatientDemographics()

        then:
        order.getFhirResourceId() == null
        order.getPatientId() == null
        order.getFirstName() == null
        order.getLastName() == null
        order.getSex() == null
        order.getBirthDateTime() == null
        order.getBirthOrder() == null
    }

    def "test argument constructor"() {
        given:
        def fhirResourceId = "12345werty"
        def patientId = "fake lab"
        def firstName = "fake hospital"
        def lastName = "lab order"
        def sex = "male"
        def birthDateTime = ZonedDateTime.now()
        def birthOrder = 1

        when:
        def demographics = new PatientDemographics(fhirResourceId, patientId, firstName, lastName, sex, birthDateTime, birthOrder)

        then:
        demographics.getFhirResourceId() == fhirResourceId
        demographics.getPatientId() == patientId
        demographics.getFirstName() == firstName
        demographics.getLastName() == lastName
        demographics.getSex() == sex
        demographics.getBirthDateTime() == birthDateTime
        demographics.getBirthOrder() == birthOrder
    }
}
