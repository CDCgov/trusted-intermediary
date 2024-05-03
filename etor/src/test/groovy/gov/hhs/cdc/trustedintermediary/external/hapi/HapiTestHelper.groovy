package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

import java.util.stream.Stream

class HapiTestHelper {
    static <T extends Resource> Stream<T> resourceInBundle(Bundle bundle, Class<T> resourceType) {
        return HapiHelper.resourcesInBundle(bundle, resourceType)
    }
}
