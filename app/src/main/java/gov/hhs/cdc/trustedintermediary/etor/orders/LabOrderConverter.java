package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;

/** Interface for converting a demographics object into a lab order. */
public interface LabOrderConverter {
    LabOrder<?> convertToOrder(Demographics<?> demographics);
}
