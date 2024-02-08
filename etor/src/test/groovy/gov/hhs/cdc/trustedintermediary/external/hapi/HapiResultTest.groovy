package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class HapiResultTest extends Specification{
	def "getUnderlyingResult works"() {
		given:
		def expectedResult = new Bundle()
		def result = new HapiResult(expectedResult)

		when:
		def actualResult = result.getUnderlyingResult();

		then:
		actualResult == expectedResult
	}

	def "getFhirResourceId works"() {
		given:
		def expectId = "fhirResourceId"
		def innerResult = new Bundle()
		innerResult.setId(expectId)

		when:
		def actualResult = new HapiResult(innerResult)

		then:
		actualResult.getFhirResourceId() == expectId
	}

}
