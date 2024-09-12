package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;

//        The signature has to change from
//          TransformationRuleMethod(String name, Map<String, String> args)
//        to something like
//          TransformationRuleMethod(String name, Map<String, Object> args) or instead of
//        Object some Java generic like
//          TransformationRuleMethod(String name, Map<String, ?> args)
public class MapLocalObservationCodes implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var observations = HapiHelper.resourcesInBundle(bundle, Observation.class);

        //        CDPH local code
        //        99717-32

        //        Suggested LOINC code
        //        85269-9
        //        Suggested LOINC description
        //        X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation

        //        List<String> definedValues = new
        //                "99717-32",
        //        "99717-33",
        //        "99717-34",
        //
        //        "99717-6",
        //
        //        "99717-35",
        //        "99717-36",
        //
        //        "99717-48",
        //        "99717-44",
        //
        //        "99717-50",
        //
        //        "99717-47",
        //        "99717-46"
        //    ];

        for (Observation obv : observations.toList()) {
            // get the 99717- prefixed value

            var coding = obv.getCode().getCodingFirstRep();

            if (coding.getSystem().equals(HapiHelper.LOCALLY_DEFINED_CODE)) {
                // update the values
            }
        }

        var dummyLoinc = "55555";
    }
}
