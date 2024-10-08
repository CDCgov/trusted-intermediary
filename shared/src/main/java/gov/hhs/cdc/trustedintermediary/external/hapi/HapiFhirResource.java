package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** An implementation of {@link HealthData} to use as a wrapper around HAPI FHIR IBaseResource */
public class HapiFhirResource implements HealthData<IBaseResource> {

    private final IBaseResource innerResource;

    public HapiFhirResource(IBaseResource innerResource) {
        this.innerResource = innerResource;
    }

    @Override
    public IBaseResource getUnderlyingData() {
        return innerResource;
    }
}
