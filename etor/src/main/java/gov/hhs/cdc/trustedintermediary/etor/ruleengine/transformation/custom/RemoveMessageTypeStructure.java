package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/** Removes the Message Structure (MSH-9.3) from the Message Type (MSH-9). */
public class RemoveMessageTypeStructure implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, Object> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        String msh9_3 = HapiHelper.getMSH9_3Value(bundle);
        if (msh9_3 == null) {
            return;
        }
        String delimiter = "^";
        List<String> displayList = new ArrayList<>(Arrays.asList(msh9_3.split("\\" + delimiter)));
        if (displayList.size() < 3) {
            return;
        }
        // Remove the third element from the list
        displayList.remove(2);
        String strippedString = String.join(delimiter, displayList);
        HapiHelper.setMSH9_3Value(bundle, strippedString);
    }
}
