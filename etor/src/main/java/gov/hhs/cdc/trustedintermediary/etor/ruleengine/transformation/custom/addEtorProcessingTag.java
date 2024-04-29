package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import static gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageConverterHelper.findOrInitializeMessageHeader;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;

public class addEtorProcessingTag implements CustomFhirTransformation {

    @Override
    public FhirResource<?> transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var messageHeader = findOrInitializeMessageHeader(bundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        var systemValue = "http://localcodes.org/ETOR";
        var codeValue = "ETOR";
        var displayValue = "Processed by ETOR";

        if (meta.getTag(systemValue, codeValue) == null) {
            meta.addTag(new Coding(systemValue, codeValue, displayValue));
        }

        messageHeader.setMeta(meta);

        return resource;
    }
}
