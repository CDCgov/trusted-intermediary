package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

/**
 * A concrete implementation of a {@link LabOrder} that uses the Hapi FHIR bundle as its underlying
 * type.
 */
public class HapiLabOrder implements LabOrder<Bundle> {

    private final Bundle innerLabOrder;

    public HapiLabOrder(Bundle innerLabOrder) {
        this.innerLabOrder = innerLabOrder;
    }

    @Override
    public Bundle getUnderlyingOrder() {
        return innerLabOrder;
    }

    @Override
    public String getFhirResourceId() {
        return innerLabOrder.getId();
    }

    @Override
    public String getPatientId() {
        return HapiHelper.resourcesInBundle(innerLabOrder, Patient.class)
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }
}
