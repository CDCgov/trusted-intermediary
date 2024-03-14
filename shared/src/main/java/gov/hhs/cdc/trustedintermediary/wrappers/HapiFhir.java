package gov.hhs.cdc.trustedintermediary.wrappers;

import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * An interface that wraps around the Hapi FHIR library. It is Hapi-specific because making it
 * library-agnostic was going to greatly complicate its use. It was decided to be more pragmatic to
 * keep the complication down.
 */
public interface HapiFhir {

    <T extends IBaseResource> T parseResource(String fhirResource, Class<T> clazz)
            throws FhirParseException;

    String encodeResourceToJson(Object resource);

    Boolean evaluateCondition(String resource, String expression);
}
