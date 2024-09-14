package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.StringType;

//        The signature has to change from
//          TransformationRuleMethod(String name, Map<String, String> args)
//        to something like
//          TransformationRuleMethod(String name, Map<String, Object> args) or instead of
//        Object some Java generic like
//          TransformationRuleMethod(String name, Map<String, ?> args)
public class MapLocalObservationCodes implements CustomFhirTransformation {

    private HashMap<String, Identifier> map;

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
                if (Objects.equals(
                                coding.getExtensionByUrl(HapiHelper.EXTENSION_CWE_CODING)
                                        .getValue()
                                        .toString(),
                                "alt-coding")
                        && coding.getSystem().equals(HapiHelper.LOCALLY_DEFINED_CODE)) {
                    // look up the code in the hash map
                    var identifier = map.get(coding.getCode());

                    // assuming for now that we found it. now create a new coding and add it to the
                    // coding list
                    var mappedCoding =
                            new Coding(
                                    identifier.codingSystem(),
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

    private void InitMap() {
        this.map = new HashMap<String, Identifier>();
        map.put(
                "99717-32",
                new Identifier(
                        "85269-9",
                        "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                        "LN"));
        // "99717-33"
        map.put(
                "99717-34",
                new Identifier(
                        "PLT325",
                        "ABCD1 gene mutation found [Identifier] in DBS by Sequencing",
                        "PLT"));
        // "99717-6"
        //
        // "99717-35"
        // "99717-36"
        //
        // "99717-48"
        // "99717-44"
        //
        // "99717-50"
        //
        // "99717-47"
        // "99717-46"
        //
        // "99717-60"
    }
}
