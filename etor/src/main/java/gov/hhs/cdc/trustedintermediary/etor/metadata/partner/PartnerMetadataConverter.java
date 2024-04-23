package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata;
import java.util.Set;

public interface PartnerMetadataConverter {

    /**
     * This method will convert {@link PartnerMetadata} and convert it into an {@link
     * org.hl7.fhir.r4.model.OperationOutcome}
     *
     * @param metadata The metadata to convert
     * @param requestedId The id of the operation outcome
     * @param messageIdsToLink The message ids to link
     * @return The OperationOutcome FHIR resource with the metadata
     */
    FhirMetadata<?> extractPublicMetadataToOperationOutcome(
            PartnerMetadata metadata, String requestedId, Set<String> messageIdsToLink);
}
