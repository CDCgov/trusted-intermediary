package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;

/** Removes the Message Structure (MSH-9.3) from the Message Type (MSH-9). */
public class removeMessageTypeStructure implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args)
            throws RuleExecutionException {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        Coding coding = HapiHelper.getMessageTypeCoding(bundle);
        String display = coding.getDisplay();
        String delimiter = "^";
        String[] displayArray = display.split("\\" + delimiter);
        ArrayList<String> displayList = new ArrayList<>(Arrays.asList(displayArray));
        if (displayList.size() < 3) {
            return;
        }
        displayList.remove(2);
        String strippedString = String.join(delimiter, displayList);
        coding.setDisplay(strippedString);
    }
}
