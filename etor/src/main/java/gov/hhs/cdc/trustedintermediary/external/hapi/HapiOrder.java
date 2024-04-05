package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

/**
 * A concrete implementation of a {@link Order} that uses the Hapi FHIR bundle as its underlying
 * type.
 */
public class HapiOrder extends HapiMessage implements Order<Bundle> {

    public HapiOrder(Bundle innerOrder) {
        super(innerOrder);
    }

    @Override
    public String getPatientId() {
        return HapiHelper.resourcesInBundle(innerResource, Patient.class)
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
