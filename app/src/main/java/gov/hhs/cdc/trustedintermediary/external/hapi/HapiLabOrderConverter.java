package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographics;
import org.hl7.fhir.r4.model.Bundle;

public class HapiLabOrderConverter implements LabOrderConverter {
    @Override
    public LabOrder<Bundle> convertToOrder(final PatientDemographics demographics) {
        var labOrder = new Bundle();
        labOrder.setId(demographics.getFhirResourceId());
        // other conversions to create the FHIR service request

        return new HapiLabOrder(labOrder);
    }
}
