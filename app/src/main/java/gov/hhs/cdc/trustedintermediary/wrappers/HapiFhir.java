package gov.hhs.cdc.trustedintermediary.wrappers;

import java.util.Optional;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;

public interface HapiFhir {
    <T extends IBase> Optional<T> fhirPathEvaluateFirst(
            IBase fhirResource, String fhirPath, Class<T> clazz);

    <T extends IBaseResource> T parseResource(String fhirResource, Class<T> clazz);

    String encodeResourceToJson(IBaseResource resource);
}
