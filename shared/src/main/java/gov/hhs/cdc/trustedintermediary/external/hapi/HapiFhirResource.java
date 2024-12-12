package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** An implementation of {@link HealthData} to use as a wrapper around HAPI FHIR IBaseResource */
public class HapiFhirResource implements HealthData<IBaseResource> {

    @Inject private Logger logger; // this line will get deleted once PR is apporved.
    private final IBaseResource innerResource;

    public HapiFhirResource(IBaseResource innerResource) {
        ApplicationContext.injectIntoNonSingleton(
                this); // this line will get deleted once PR is apporved.
        this.innerResource = innerResource;
    }

    @Override
    public IBaseResource getUnderlyingData() {
        this.logger.logDebug("testing @Inject"); // this line will get deleted once PR is apporved.
        return innerResource;
    }
}
