package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import org.hl7.fhir.r4.model.OperationOutcome
import spock.lang.Specification

import java.time.Instant

class HapiMetadataConverterTest extends Specification {
    def "creating an issue returns a valid OperationOutcomeIssueComponent with Information level severity and code" () {
        when:
        def output = HapiPartnerMetadataConverter.getInstance().createInformationIssueComponent("test_details", "test_diagnostics")
        then:
        output.getSeverity() == OperationOutcome.IssueSeverity.INFORMATION
        output.getCode() == OperationOutcome.IssueType.INFORMATIONAL
        output.getDetails().getText() == "test_details"
        output.getDiagnostics() == "test_diagnostics"
    }

    def "ExtractPublicMetadata to OperationOutcome returns FHIR metadata"() {
        given:

        def sendingApp = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
        def sendingFacility = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
        def receivingApp = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
        def receivingFacility = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")
        def time = Instant.now()
        def hash = "hash"
        def failureReason = "timed_out"
        def messageType =  PartnerMetadataMessageType.ORDER
        PartnerMetadata metadata = new PartnerMetadata(
                "receivedSubmissionId", "sentSubmissionId", time, time, hash, PartnerMetadataStatus.DELIVERED, failureReason, messageType, sendingApp, sendingFacility, receivingApp, receivingFacility, "placer_order_number")

        when:
        def result = HapiPartnerMetadataConverter.getInstance().extractPublicMetadataToOperationOutcome(metadata, "receivedSubmissionId").getUnderlyingOutcome() as OperationOutcome

        then:
        result.getId() == "receivedSubmissionId"
        result.getIssue().get(0).diagnostics == sendingFacility.namespace()
        result.getIssue().get(1).diagnostics == receivingFacility.namespace()
        result.getIssue().get(2).diagnostics == time.toString()
        result.getIssue().get(3).diagnostics == hash
        result.getIssue().get(4).diagnostics == time.toString()
        result.getIssue().get(5).diagnostics == PartnerMetadataStatus.DELIVERED.toString()
        result.getIssue().get(6).diagnostics == failureReason
        result.getIssue().get(7).diagnostics == messageType.toString()
        result.getIssue().get(8).diagnostics == "sentSubmissionId"
        result.getIssue().get(9).diagnostics == "receivedSubmissionId"
    }
}
