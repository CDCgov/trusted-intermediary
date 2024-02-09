package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class HapiMessageConverterHelperTest extends Specification {

	Bundle mockBundle
	Patient mockPatient
	ResultMock resultMock

	def setup() {
		TestApplicationContext.reset()
		TestApplicationContext.init()
		TestApplicationContext.register(HapiMessageConverterHelper, HapiMessageConverterHelper.getInstance())
		TestApplicationContext.injectRegisteredImplementations()

		mockPatient = new Patient()
		mockBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
	}

	def "addEtorTag adds the ETOR message header tag for Orders"() {
		given:
		def expectedSystem = "http://localcodes.org/ETOR"
		def expectedCode = "ETOR"
		def expectedDisplay = "Processed by ETOR"

		when:
		HapiMessageConverterHelper.getInstance().addEtorTag(mockBundle)

		then:
		def messageHeaders = resultMock
		def actualSystem = messageHeaders.getMeta().getTag()[0].getSystem()
		def actualCode = messageHeaders.getMeta().getTag()[0].getCode()
		def actualDisplay = messageHeaders.getMeta().getTag()[0].getDisplay()
	}
}
