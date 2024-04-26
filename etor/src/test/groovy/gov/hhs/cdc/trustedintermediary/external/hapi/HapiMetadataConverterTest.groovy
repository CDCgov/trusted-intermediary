package gov.hhs.cdc.trustedintermediary.external.hapi

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataMessageType
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus
import org.hl7.fhir.r4.model.OperationOutcome
import spock.lang.Specification

import java.time.Instant

class HapiMetadataConverterTest extends Specification {
    def sendingAppDetails = new MessageHdDataType("sending_app_name", "sending_app_id", "sending_app_type")
    def sendingFacilityDetails = new MessageHdDataType("sending_facility_name", "sending_facility_id", "sending_facility_type")
    def receivingAppDetails = new MessageHdDataType("receiving_app_name", "receiving_app_id", "receiving_app_type")
    def receivingFacilityDetails = new MessageHdDataType("receiving_facility_name", "receiving_facility_id", "receiving_facility_type")

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

        def time = Instant.now()
        def hash = "hash"
        def failureReason = "timed_out"
        def messageType =  PartnerMetadataMessageType.ORDER
        def messageIds = Set.of("TestId")
        PartnerMetadata metadata = new PartnerMetadata(
                "receivedSubmissionId", "sentSubmissionId", time, time, hash, PartnerMetadataStatus.DELIVERED, failureReason, messageType, sendingAppDetails, sendingFacilityDetails, receivingAppDetails, receivingFacilityDetails, "placer_order_number")

        when:
        def result = HapiPartnerMetadataConverter.getInstance().extractPublicMetadataToOperationOutcome(metadata, "receivedSubmissionId", messageIds).getUnderlyingOutcome() as OperationOutcome

        then:
        result.getId() == "receivedSubmissionId"
        result.getIssue().get(0).diagnostics == messageIds.toString()
        result.getIssue().get(1).diagnostics == sendingFacilityDetails.universalId()
        result.getIssue().get(2).diagnostics == receivingFacilityDetails.universalId()
        result.getIssue().get(3).diagnostics == time.toString()
        result.getIssue().get(4).diagnostics == hash
        result.getIssue().get(5).diagnostics == time.toString()
        result.getIssue().get(6).diagnostics == PartnerMetadataStatus.DELIVERED.toString()
        result.getIssue().get(7).diagnostics == failureReason
        result.getIssue().get(8).diagnostics == messageType.toString()
        result.getIssue().get(9).diagnostics == "sentSubmissionId"
        result.getIssue().get(10).diagnostics == "receivedSubmissionId"
    }
}
