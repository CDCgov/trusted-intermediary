package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

/** Helper class that works on HapiFHIR constructs. */
public class HapiHelper {

    /**
     * Returns a {@link Stream} of FHIR resources inside the provided {@link Bundle} that match the
     * given resource type.
     *
     * @param bundle The bundle to search.
     * @param resourceType The class of the resource to search for.
     * @param <T> The type that either is or extends {@link Resource}.
     * @return The stream of the given resource type.
     */
    public static <T extends Resource> Stream<T> resourcesInBundle(
            Bundle bundle, Class<T> resourceType) {
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource.getClass().equals(resourceType))
                .map(resource -> ((T) resource));
    }
}
