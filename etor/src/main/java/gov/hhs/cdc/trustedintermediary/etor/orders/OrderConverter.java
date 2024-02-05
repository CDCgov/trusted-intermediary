package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata;

/** Interface for converting things to orders and things in orders. */
public interface OrderConverter {
    Order<?> convertToOrder(Demographics<?> demographics);

    Order<?> convertToOmlOrder(Order<?> order);

    Order<?> addContactSectionToPatientResource(Order<?> order);

    Order<?> addEtorProcessingTag(Order<?> message);

    FhirMetadata<?> extractPublicMetadataToOperationOutcome(
            PartnerMetadata metadata, String requestedId);
}
