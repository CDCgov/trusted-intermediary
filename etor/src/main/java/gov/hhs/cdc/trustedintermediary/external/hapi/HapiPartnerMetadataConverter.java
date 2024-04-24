package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataConverter;
import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata;
import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.HapiFhirMetadata;
import java.util.Set;
import org.hl7.fhir.r4.model.OperationOutcome;

public class HapiPartnerMetadataConverter implements PartnerMetadataConverter {

    private static final HapiPartnerMetadataConverter INSTANCE = new HapiPartnerMetadataConverter();

    public static HapiPartnerMetadataConverter getInstance() {
        return INSTANCE;
    }

    private HapiPartnerMetadataConverter() {}

    @Override
    public FhirMetadata<?> extractPublicMetadataToOperationOutcome(
            PartnerMetadata metadata, String requestedId, Set<String> messageIdsToLink) {
        var operation = new OperationOutcome();

        operation.setId(requestedId);

        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "linked messages", messageIdsToLink.toString()));

        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "sender universal id",
                                metadata.sendingFacilityDetails().universalId()));
        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "receiver universal id",
                                metadata.receivingFacilityDetails().universalId()));

        String ingestion = null;
        String delivered = null;
        if (metadata.timeReceived() != null) {
            ingestion = metadata.timeReceived().toString();
        }
        if (metadata.timeDelivered() != null) {
            delivered = metadata.timeDelivered().toString();
        }

        operation.getIssue().add(createInformationIssueComponent("ingestion", ingestion));

        operation.getIssue().add(createInformationIssueComponent("payload hash", metadata.hash()));
        operation.getIssue().add(createInformationIssueComponent("delivered", delivered));
        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "delivery status", metadata.deliveryStatus().toString()));

        operation
                .getIssue()
                .add(createInformationIssueComponent("status message", metadata.failureReason()));

        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "message type", metadata.messageType().toString()));

        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "outbound submission id", metadata.sentSubmissionId()));

        operation
                .getIssue()
                .add(
                        createInformationIssueComponent(
                                "inbound submission id", metadata.receivedSubmissionId()));

        return new HapiFhirMetadata(operation);
    }

    protected OperationOutcome.OperationOutcomeIssueComponent createInformationIssueComponent(
            String details, String diagnostics) {
        OperationOutcome.OperationOutcomeIssueComponent issue =
                new OperationOutcome.OperationOutcomeIssueComponent();

        issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
        issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
        issue.getDetails().setText(details);
        issue.setDiagnostics(diagnostics);

        return issue;
    }
}
