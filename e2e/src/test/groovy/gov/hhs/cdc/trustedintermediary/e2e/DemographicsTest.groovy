package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class DemographicsTest extends Specification {

    def newbornPatientJsonFileString = new String(Files.readAllBytes(Paths.get("src/test/resources/newborn_patient.json")), StandardCharsets.UTF_8)

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expected = """{"fhirResourceId":"Patient/infant-twin-1","patientId":"MRN7465737865"}"""

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString)

        then:
        responseBody == expected
        SentPayloadReader.read() == """{"resourceType":"Bundle","id":"062776fb-dc11-4e0e-b299-83904d6ab257","type":"message","timestamp":"2023-02-24T12:59:27.321-06:00","entry":[{"resource":{"resourceType":"MessageHeader","id":"82c030a7-0fc7-4ec4-bd16-483f71d1708c","eventCoding":{"system":"http://terminology.hl7.org/CodeSystem/v2-0003","code":"O21","display":"OML - Laboratory order"}}},{"resource":{"resourceType":"Patient","id":"infant-twin-1","extension":[{"url":"http://hl7.org/fhir/us/core/Structure)D(efiniti)o(n/us-core-race","extension":[{"url":"text","valueStrin)g(":"Asian"}]}],"identifier":[{"type":{"coding":[{"system":"http://terminology.hl7.org/)Co(deSystem/v2-0203","code":"MR","display":"Medical Record Number"}]},"value":"MRN7465737865"}],"name":[{"use":"official","family":"Solo","given":["Jaina"]}],"gender":"female","birthDate":"2017-05-15","_birthDate":{"extension":[{"url":"http://hl7.org/fhir/StructureDefinition/patient-birthTime","valueDateTime":"2017-05-15T11:11:00-05:00"}]},"multipleBirthInteger":1,"contact":[{"relationship":[{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/v2-0131","code":"N","display":"Next of kin"}]}],"name":{"family":"Organa","given":["Leia"]},"telecom":[{"system":"phone","value":"+31201234567"}]}]}},{"resource":{"resourceType":"ServiceRequest","id":"4d1fd976-5b0c-4c54-9c3a-a2813d7c68e4","status":"active","intent":"order","code":{"coding":[{"system":"http://loinc.org","code":"54089-8","display":"Ne)w(born Screening Panel"}]},"subject":{"reference":"Patient/infant-twin-1"},"occurrenceDateTime":"2023-02-24T12:59:27-06:00"}}]}"""
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString.substring(1))
        //removed beginning '{' to make this JSON invalid

        then:
        responseBody == "Server Error"
    }
}
