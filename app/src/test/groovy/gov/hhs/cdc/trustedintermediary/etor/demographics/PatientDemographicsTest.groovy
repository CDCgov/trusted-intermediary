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
        def demographics = new PatientDemographics()

        then:
        demographics.getFhirResourceId() == null
        demographics.getPatientId() == null
        demographics.getFirstName() == null
        demographics.getLastName() == null
        demographics.getSex() == null
        demographics.getBirthDateTime() == null
        demographics.getBirthOrder() == null
        demographics.getRace() == null
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
        def race = "Asian"
        def nextOfKin = new NextOfKin("Jaina", "Solo", "555-555-5555")

        when:
        def demographics = new PatientDemographics(fhirResourceId, patientId, firstName, lastName, sex, birthDateTime, birthOrder, race, nextOfKin)

        then:
        demographics.getFhirResourceId() == fhirResourceId
        demographics.getPatientId() == patientId
        demographics.getFirstName() == firstName
        demographics.getLastName() == lastName
        demographics.getSex() == sex
        demographics.getBirthDateTime() == birthDateTime
        demographics.getBirthOrder() == birthOrder
        demographics.getRace() == race
        demographics.getNextOfKin() == nextOfKin
    }

    def "test toString"() {
        given:
        def fhirResourceId = "12345werty"
        def patientId = "fake lab"
        def firstName = "fake hospital"
        def lastName = "lab order"
        def sex = "male"
        def birthDateTime = ZonedDateTime.now()
        def birthOrder = 1
        def race = "Asian"
        def nextOfKin = new NextOfKin("Jaina", "Solo", "555-555-5555")

        when:
        def demographicsString = new PatientDemographics(fhirResourceId, patientId, firstName, lastName, sex, birthDateTime, birthOrder, race, nextOfKin).toString()

        then:
        demographicsString.contains(fhirResourceId)
        demographicsString.contains(patientId)
        demographicsString.contains(firstName)
        demographicsString.contains(lastName)
        demographicsString.contains(sex)
        demographicsString.contains(birthDateTime.toString())
        demographicsString.contains(birthOrder.toString())
        demographicsString.contains(race)
        demographicsString.contains(nextOfKin.toString())
    }
}
