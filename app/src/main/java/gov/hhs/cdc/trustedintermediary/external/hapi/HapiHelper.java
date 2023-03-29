package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class HapiHelper {
    private HapiHelper() {}

    public static <T extends Resource> Stream<T> resourcesInBundle(
            Bundle bundle, Class<T> resourceType) {
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource.getClass().equals(resourceType))
                .map(resource -> ((T) resource));
    }
}
