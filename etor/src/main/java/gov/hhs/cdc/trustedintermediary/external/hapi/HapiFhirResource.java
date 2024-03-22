package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class HapiFhirResource implements FhirResource<IBaseResource> {

    private final IBaseResource innerResource;

    public HapiFhirResource(IBaseResource innerResource) {
        this.innerResource = innerResource;
    }

    @Override
    public IBaseResource getUnderlyingResource() {
        return innerResource;
    }
}
