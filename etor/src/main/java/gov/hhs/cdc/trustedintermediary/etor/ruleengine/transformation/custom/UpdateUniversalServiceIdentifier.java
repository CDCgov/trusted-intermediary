package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;

/**
 * Override the Name of Coding System (OBR-4.3) and Alternate Identifier (OBR-4.4) In the Universal
 * Service Identifier (OBR-4)
 */
public class UpdateUniversalServiceIdentifier implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        serviceRequests.forEach(
                it -> {
                    var allCodings = it.getCode().getCoding();
                    var codingSystemContainer =
                            getCodingSystemContainer(allCodings, args.get("checkValue"));

                    if (codingSystemContainer == null) {
                        // we're only interested in coding that matches the checkValue argument
                        return;
                    }

                    // check for the coding system label and create or override it
                    updateCodingSystemLabel(codingSystemContainer, args.get("codingSystem"));

                    // the alt id is stored on a separate coding object, so we need to filter
                    // for it
                    updateAlternateCodingId(allCodings, args);
                });
    }

    /**
     * Extract the first "Coding System" object that matches a given string from a given list of
     * Codings
     */
    private Coding getCodingSystemContainer(List<Coding> allCodings, String checkValue) {
        return allCodings.stream()
                .filter(ext -> Objects.equals(ext.getCode(), checkValue))
                .findFirst()
                .orElse(null);
    }

    /** Find and create or update the "Coding System" object in a given List */
    private void updateCodingSystemLabel(Coding codingSystemContainer, String codingSystem) {
        var codingSystemLabel =
                codingSystemContainer.getExtensionByUrl(HapiHelper.EXTENSION_CODING_SYSTEM);
        if (codingSystemLabel == null) {
            codingSystemLabel = new Extension();
            codingSystemLabel.setUrl(HapiHelper.EXTENSION_CODING_SYSTEM);
            codingSystemContainer.addExtension(codingSystemLabel);
        }
        codingSystemLabel.setValue(new StringType(codingSystem));
    }

    /** Extract the first "Alternate Id" object in a given list of Codings */
    private Coding getAltCodingContainer(List<Coding> allCodings) {
        return allCodings.stream()
                .filter(
                        ext ->
                                ext.getExtensionsByUrl(HapiHelper.EXTENSION_CWE_CODING).stream()
                                        .anyMatch(
                                                cwe ->
                                                        Objects.equals(
                                                                cwe.getValue().primitiveValue(),
                                                                HapiHelper.EXTENSION_ALT_CODING)))
                .findFirst()
                .orElse(null);
    }

    /** Find and create or update the "Alternate Id" object in a given List */
    private void updateAlternateCodingId(List<Coding> allCodings, Map<String, String> args) {
        var altCodingContainer = getAltCodingContainer(allCodings);

        if (altCodingContainer == null) {
            // build a Coding object with an "alt-coding" extension
            altCodingContainer = new Coding();
            var altCodingExtension = new Extension();
            altCodingExtension.setUrl(HapiHelper.EXTENSION_CWE_CODING);
            altCodingExtension.setValue(new StringType(HapiHelper.EXTENSION_ALT_CODING));
            altCodingContainer.addExtension(altCodingExtension);
            allCodings.add(altCodingContainer);
        }
        altCodingContainer.setCode(args.get("alternateId"));
    }
}
