package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import org.hl7.fhir.r4.model.Bundle;

/**
 * A concrete implementation of a {@link Demographics} that uses the Hapi FHIR bundle as its
 * underlying type.
 */
public class HapiDemographics extends HapiMessage implements Demographics<Bundle> {

    public HapiDemographics(Bundle innerDemographics) {
        super(innerDemographics);
    }
}
