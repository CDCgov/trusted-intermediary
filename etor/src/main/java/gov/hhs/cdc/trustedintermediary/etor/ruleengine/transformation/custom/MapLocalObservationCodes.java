package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.IdentifierCode;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.StringType;

public class MapLocalObservationCodes implements CustomFhirTransformation {
    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);

    private HashMap<String, IdentifierCode> map;

    public MapLocalObservationCodes() {
        InitMap();
    }

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var observations = HapiHelper.resourcesInBundle(bundle, Observation.class);

        for (Observation obv : observations.toList()) {
            var codingList = obv.getCode().getCoding();
            var updatedList = new ArrayList<Coding>();

            for (Coding coding : codingList) {
                var cwe =
                        coding.getExtensionByUrl(HapiHelper.EXTENSION_CWE_CODING)
                                .getValue()
                                .toString();

                // @todo this wont work if coding is not the first extension. see better way of
                // doing it...
                if (Objects.equals(cwe, "coding")
                        || coding.getSystem().equals(HapiHelper.LOINC_CODE)) {
                    break;
                }

                // alt coding is HL7 OBX-3.4,5,6
                if (Objects.equals(cwe, "alt-coding")
                        && coding.getSystem().equals(HapiHelper.LOCAL_CODE_URL)) {

                    // look up the code in the hash map
                    var identifier = map.get(coding.getCode());

                    if (identifier == null) {
                        // The local code was not found in the mapping
                        logger.logWarning("Unmapped local code detected");
                        continue;
                    }

                    // Create a new coding and add it to the coding list
                    var mappedCoding =
                            new Coding(
                                    UrlForCodetype(identifier.codingSystem()),
                                    identifier.code(),
                                    identifier.display());
                    mappedCoding.addExtension(
                            HapiHelper.EXTENSION_CWE_CODING, new StringType("coding"));

                    mappedCoding.addExtension(
                            HapiHelper.EXTENSION_CODING_SYSTEM,
                            new StringType(identifier.codingSystem()));

                    updatedList.add(mappedCoding);
                }
            }

            codingList.addAll(0, updatedList);
        }
    }

    private String UrlForCodetype(String code) {
        return switch (code) {
            case HapiHelper.LOINC_CODE -> HapiHelper.LOINC_URL;
            case HapiHelper.PLT_CODE -> null;
            default -> HapiHelper.LOCAL_CODE_URL;
        };
    }

    private void InitMap() {
        this.map = new HashMap<String, IdentifierCode>();
        map.put(
                "99717-32",
                new IdentifierCode(
                        "85269-9",
                        "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                        HapiHelper.LOINC_CODE));
        map.put(
                "99717-33",
                new IdentifierCode(
                        "85268-1",
                        "X-linked Adrenoleukodystrophy (X- ALD) newborn screening comment-discussion",
                        HapiHelper.LOINC_CODE));
        map.put(
                "99717-34",
                new IdentifierCode(
                        "PLT325",
                        "ABCD1 gene mutation found [Identifier] in DBS by Sequencing",
                        HapiHelper.PLT_CODE));
        map.put(
                "99717-6",
                new IdentifierCode(
                        "53340-6",
                        "17-Hydroxyprogesterone [Moles/volume] in DBS",
                        HapiHelper.LOINC_CODE));
        map.put("99717-35", new IdentifierCode("REQUEST_PLT", "REQUEST_PLT", HapiHelper.PLT_CODE));
        map.put("99717-36", new IdentifierCode("REQUEST PLT", "REQUEST_PLT", HapiHelper.PLT_CODE));
        map.put(
                "99717-48",
                new IdentifierCode(
                        "PLT3258",
                        "IDUA gene mutations found [Identifier] in DBS by Sequencing",
                        HapiHelper.PLT_CODE));
        map.put("99717-44", new IdentifierCode("REQUEST PLT", "REQUEST PLT", HapiHelper.PLT_CODE));
        map.put(
                "99717-50",
                new IdentifierCode(
                        "PLT3275",
                        "IDS gene mutations found [Identifier] in Dried Bloodspot by Molecular genetics method Nominal",
                        HapiHelper.PLT_CODE));
        map.put(
                "99717-47",
                new IdentifierCode(
                        "PLT3252",
                        "GAA gene mutation found [Identifier] in DBS by Sequencing",
                        HapiHelper.PLT_CODE));
        map.put("99717-46", new IdentifierCode("REQUEST PLT", "REQUEST PLT", HapiHelper.PLT_CODE));
        map.put("99717-60", new IdentifierCode("REQUEST PLT", "REQUEST PLT", HapiHelper.PLT_CODE));
    }
}
