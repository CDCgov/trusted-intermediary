package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.PojoTestUtils
import spock.lang.Specification

class NextOfKinTest extends Specification {
    def "test getters and setters"() {
        when:
        PojoTestUtils.validateGettersAndSetters(NextOfKin)

        then:
        noExceptionThrown()
    }

    def "test argument constructor"() {
        given:
        def firstName = "Jaina"
        def lastName = "Solo"
        def phoneNumber = "555-867-5309"

        when:
        def nextOfKin = new NextOfKin(firstName, lastName, phoneNumber)

        then:
        nextOfKin.getFirstName() == firstName
        nextOfKin.getLastName() == lastName
        nextOfKin.getPhoneNumber() == phoneNumber
    }

    def "test toString"() {
        given:
        def firstName = "Jaina"
        def lastName = "Solo"
        def phoneNumber = "555-867-5309"

        when:
        def nextOfKinString = new NextOfKin(firstName, lastName, phoneNumber).toString()

        then:
        nextOfKinString.contains(firstName)
        nextOfKinString.contains(lastName)
        nextOfKinString.contains(phoneNumber)
    }
}
