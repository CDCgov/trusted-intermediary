package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import org.hl7.fhir.r4.model.Bundle;

/** Filler concrete implementation of a {@link Result} using the Hapi FHIR library */
public class HapiResult extends HapiMessage implements Result<Bundle> {

    public HapiResult(Bundle innerResult) {
        super(innerResult);
    }
}
