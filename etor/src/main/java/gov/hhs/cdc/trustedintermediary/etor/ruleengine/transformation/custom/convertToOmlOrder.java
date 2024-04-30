package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import static gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageConverterHelper.findOrInitializeMessageHeader;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;

public class convertToOmlOrder implements CustomFhirTransformation {
    private final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();

        var messageHeader = findOrInitializeMessageHeader(bundle);
        messageHeader.setEvent(OML_CODING);
    }
}
