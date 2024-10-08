package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
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

    public static final String CHECK_VALUE_NAME = "checkValue";
    public static final String CODING_SYSTEM_NAME = "codingSystem";
    public static final String ALTERNATE_ID_NAME = "alternateId";

    @Override
    public void transform(HealthData<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingData();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        // Let it fail if args.get("<property>") is not a string
        serviceRequests.forEach(
                it -> {
                    var allCodings = it.getCode().getCoding();
                    var codingSystemContainer =
                            getCodingSystemContainer(
                                    allCodings, (String) args.get(CHECK_VALUE_NAME));

                    if (codingSystemContainer == null) {
                        // we're only interested in coding that matches the checkValue argument
                        return;
                    }

                    // check for the coding system label and create or override it
                    updateCodingSystemLabel(
                            codingSystemContainer, (String) args.get(CODING_SYSTEM_NAME));

                    // the alt id is stored on a separate coding object, so we need to filter
                    // for it
                    String alternateId = (String) args.get(ALTERNATE_ID_NAME);
                    if (alternateId != null) {
                        updateAlternateCodingId(allCodings, alternateId);
                    }
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
    private void updateAlternateCodingId(List<Coding> allCodings, String alternateId) {
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
        altCodingContainer.setCode(alternateId);
    }
}
