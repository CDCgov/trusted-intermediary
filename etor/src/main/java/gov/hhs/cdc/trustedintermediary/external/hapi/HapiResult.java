package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.ServiceRequest;

/** Filler concrete implementation of a {@link Result} using the Hapi FHIR library */
public class HapiResult implements Result<Bundle> {

    private final Bundle innerResult;

    public HapiResult(Bundle innerResult) {
        this.innerResult = innerResult;
    }

    @Override
    public Bundle getUnderlyingResult() {
        return innerResult;
    }

    @Override
    public String getFhirResourceId() {
        return innerResult.getId();
    }

    @Override
    public String getPlacerOrderNumber() {
        return HapiHelper.resourcesInBundle(innerResult, ServiceRequest.class)
                .flatMap(serviceRequest -> serviceRequest.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "PLAC"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getSendingApplicationId() {
        return HapiHelper.resourcesInBundle(innerResult, MessageHeader.class)
                .flatMap(header -> header.getSource().getExtension().stream())
                .filter(
                        extension ->
                                "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id"
                                        .equals(extension.getUrl()))
                .map(extension -> extension.getValue().toString())
                .findFirst()
                .orElse("");
    }

    @Override
    public String getSendingFacilityId() {
        return null;
    }

    @Override
    public String getReceivingApplicationId() {
        return null;
    }

    @Override
    public String getReceivingFacilityId() {
        return null;
    }
}
