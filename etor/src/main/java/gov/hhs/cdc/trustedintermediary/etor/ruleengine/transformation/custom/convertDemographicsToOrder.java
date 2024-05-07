package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Custom transformation to convert a demographics message to an order. This class may be removed if
 * we don't need to handle demographics.
 */
public class convertDemographicsToOrder implements CustomFhirTransformation {

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        HapiOrderConverter.convertDemographicsToOrder(bundle);
    }
}
