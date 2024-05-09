package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/** Custom transformation to add an ETOR tag to the message header of a FHIR bundle. */
public class addEtorProcessingTag implements CustomFhirTransformation {

    private final MetricMetadata metadata =
            ApplicationContext.getImplementation(MetricMetadata.class);

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();

        var system = "http://localcodes.org/ETOR";
        var code = "ETOR";
        var display = "Processed by ETOR";

        HapiHelper.addMetaTag(bundle, system, code, display);

        metadata.put(bundle.getId(), EtorMetadataStep.ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER);
    }
}
