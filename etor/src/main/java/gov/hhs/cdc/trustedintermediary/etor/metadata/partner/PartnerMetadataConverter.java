package gov.hhs.cdc.trustedintermediary.etor.metadata.partner;

import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata;

public interface PartnerMetadataConverter {

    /**
     * This method will convert {@link PartnerMetadata} and convert it into an {@link
     * org.hl7.fhir.r4.model.OperationOutcome}
     *
     * @param metadata
     * @param requestedId
     * @return
     */
    FhirMetadata<?> extractPublicMetadataToOperationOutcome(
            PartnerMetadata metadata, String requestedId);
}
