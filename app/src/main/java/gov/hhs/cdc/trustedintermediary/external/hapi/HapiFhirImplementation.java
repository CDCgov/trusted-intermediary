package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Concrete implementation that calls the Hapi FHIR library. */
public class HapiFhirImplementation implements HapiFhir {

    private static final HapiFhirImplementation INSTANCE = new HapiFhirImplementation();

    private static final FhirContext CONTEXT = FhirContext.forR4();
    private static final IFhirPath PATHER = CONTEXT.newFhirPath();

    private HapiFhirImplementation() {}

    public static HapiFhirImplementation getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends IBase> Optional<T> fhirPathEvaluateFirst(
            final IBase fhirResource, final String fhirPath, final Class<T> clazz) {
        return PATHER.evaluateFirst(fhirResource, fhirPath, clazz);
    }

    @Override
    public <T extends IBaseResource> T parseResource(
            final String fhirResource, final Class<T> clazz) {
        IParser resourceParser = CONTEXT.newJsonParser();
        return resourceParser.parseResource(clazz, fhirResource);
    }

    @Override
    public String encodeResourceToJson(Object resource) {
        IParser encodeResourceParser = CONTEXT.newJsonParser();
        return encodeResourceParser.encodeResourceToString((IBaseResource) resource);
    }
}
