package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** An implementation of {@link FhirResource} to use as a wrapper around HAPI FHIR IBaseResource */
public class HapiFhirResource implements FhirResource<IBaseResource> {

    private final IBaseResource innerResource;

    public HapiFhirResource(IBaseResource innerResource) {
        this.innerResource = innerResource;
    }

    @Override
    public IBaseResource getUnderlyingResource() {
        return innerResource;
    }

    @Override
    public String getFhirResourceId() {
        return innerResource.getIdElement().getIdPart();
    }
}
