package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import org.hl7.fhir.r4.model.Bundle;

/**
 * A concrete implementation of a {@link Order} that uses the Hapi FHIR bundle as its underlying
 * type.
 */
public class HapiOrder extends HapiMessage implements Order<Bundle> {

    public HapiOrder(Bundle innerOrder) {
        super(innerOrder);
    }
}
