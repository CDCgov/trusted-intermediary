package gov.hhs.cdc.trustedintermediary.external.hapi


import gov.hhs.cdc.trustedintermediary.ResultMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.results.Result
import gov.hhs.cdc.trustedintermediary.etor.results.ResultConverter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Patient
import spock.lang.Specification

class HapiResultConverterTest extends Specification {
	Bundle mockResultBundle
	Patient mockPatient
	Result<Bundle> mockResult

	def setup() {
		TestApplicationContext.reset()
		TestApplicationContext.init()
		TestApplicationContext.register(ResultConverter, HapiResultConverter.getInstance())
		TestApplicationContext.register(HapiMessageConverterHelper, HapiMessageConverterHelper.getInstance())
		TestApplicationContext.injectRegisteredImplementations()

		mockPatient = new Patient()
		mockResultBundle = new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(mockPatient))
		mockResult = new ResultMock("mockFhirResourceId", mockResultBundle)
	}

	def "add etor processing tag to messageHeader resource"() {
		given:
		def expectedSystem = "http://localcodes.org/ETOR"
		def expectedCode = "ETOR"
		def expectedDisplay = "Processed by ETOR"

		def messageHeader = new MessageHeader()
		messageHeader.setId(UUID.randomUUID().toString())
		def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
		mockResultBundle.getEntry().add(1, messageHeaderEntry)
		mockResult.getUnderlyingResult() >> mockResultBundle

		when:
		def convertedResultBundle = HapiResultConverter.getInstance().addEtorProcessingTag(mockResult).getUnderlyingResult() as Bundle

		then:
		def messageHeaders = convertedResultBundle.getEntry().get(1).getResource() as MessageHeader
		def actualSystem = messageHeaders.getMeta().getTag()[0].getSystem()
		def actualCode = messageHeaders.getMeta().getTag()[0].getCode()
		def actualDisplay = messageHeaders.getMeta().getTag()[0].getDisplay()
		actualSystem == expectedSystem
		actualCode == expectedCode
		actualDisplay == expectedDisplay
	}

}
