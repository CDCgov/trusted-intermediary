package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;

/**
 * A concrete implementation of a {@link Demographics} that uses the Hapi FHIR bundle as its
 * underlying type.
 */
public class HapiDemographics implements Demographics<Bundle> {

    private final Bundle innerDemographics;

    public HapiDemographics(Bundle innerDemographics) {
        this.innerDemographics = innerDemographics;
    }

    @Override
    public Bundle getUnderlyingDemographics() {
        return innerDemographics;
    }

    @Override
    public String getFhirResourceId() {
        return innerDemographics.getId();
    }

    @Override
    public String getPatientId() {
        return innerDemographics.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
                .map(resource -> ((Patient) resource))
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }
}
