package gov.hhs.cdc.trustedintermediary.external.hapi

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

        def sender = "sender"
        def receiver = "receiver"
        def time = Instant.now()
        def hash = "hash"
        def failureReason = "timed_out"
        def messageType =  PartnerMetadataMessageType.ORDER
        PartnerMetadata metadata = new PartnerMetadata(
                "receivedSubmissionId", "sentSubmissionId", sender, receiver, time, time, hash, PartnerMetadataStatus.DELIVERED, failureReason, messageType)

        when:
        def result = HapiPartnerMetadataConverter.getInstance().extractPublicMetadataToOperationOutcome(metadata, "receivedSubmissionId").getUnderlyingOutcome() as OperationOutcome

        then:
        result.getId() == "receivedSubmissionId"
        result.getIssue().get(0).diagnostics == sender
        result.getIssue().get(1).diagnostics == receiver
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
