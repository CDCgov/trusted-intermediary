package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographics
import spock.lang.Specification

class HapiLabOrderConverterTest extends Specification {
    def "conversion between demographics and the LabOrder is correct"() {
        given:
        def demographics = new PatientDemographics(
                'fhir123id',
                'patient123id',
                'John',
                'Doe',
                'M',
                null,
                null,
                null,
                null
                )

        when:
        def labOrder = HapiLabOrderConverter.getInstance().convertToOrder(demographics).getUnderlyingOrder()

        then:
        labOrder.getId() == demographics.getFhirResourceId()
    }
}
