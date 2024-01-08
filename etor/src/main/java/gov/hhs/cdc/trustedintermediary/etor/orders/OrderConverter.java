package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import org.hl7.fhir.r4.model.OperationOutcome;

/** Interface for converting things to orders and things in orders. */
public interface OrderConverter {
    Order<?> convertToOrder(Demographics<?> demographics);

    Order<?> convertMetadataToOmlOrder(Order<?> order);

    Order<?> addContactSectionToPatientResource(Order<?> order);

    OperationOutcome extractPublicMetadataToOperationOutcome(PartnerMetadata metadata);
}
