package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ResultTest extends Specification {
	def resultClient = new EndpointClient("/v1/etor/results")
	def labResultJsonFileString = Files.readString(Path.of("../examples/MN/004_MN_ORU_R01_NBS_translation_from_initial_hl7_ingestion.fhir"))
	def submissionId = "submissionId"

	def setup() {
		SentPayloadReader.delete()
	}

	def "a result response is returned from the ETOR order endpoint"() {
		given:
		def expectedFhirResourceId  = "Bundle/1705511861639940150.e2c69100-24af-4bbd-86bf-2f29be816edf"

		when:
		def response = resultClient.submit(labResultJsonFileString, submissionId, true)
		def parsedJsonBody = JsonParsing.parseContent(response)

		then:
		response.getCode() == 200
		parsedJsonBody.fhirResourceId == expectedFhirResourceId
	}

	def "check that the rest of the message is unchanged except the parts we changed"() {
		when:
		resultClient.submit(labResultJsonFileString, submissionId, true)
		def sentPayload = SentPayloadReader.read()
		def parsedSentPayload = JsonParsing.parse(sentPayload)
		def parsedLabResultJsonFile = JsonParsing.parse(labResultJsonFileString)

		then:

		parsedSentPayload == parsedLabResultJsonFile
	}
}
