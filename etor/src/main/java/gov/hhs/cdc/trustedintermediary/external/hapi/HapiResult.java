package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Bundle;

/** Filler concrete implementation of a {@link Result} using the Hapi FHIR library */
public class HapiResult implements Result<Bundle> {

    private final HapiMessageHelper MESSAGE_HELPER =
            ApplicationContext.getImplementation(HapiMessageHelper.class);

    private final Bundle innerResult;

    public HapiResult(Bundle innerResult) {
        this.innerResult = innerResult;
    }

    @Override
    public Bundle getUnderlyingResource() {
        return innerResult;
    }

    @Override
    public String getFhirResourceId() {
        return innerResult.getId();
    }

    @Override
    public String getPlacerOrderNumber() {
        return MESSAGE_HELPER.extractPlacerOrderNumber(innerResult);
    }

    @Override
    public MessageHdDataType getSendingApplicationDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractSendingApplicationNamespace(innerResult),
                () -> MESSAGE_HELPER.extractSendingApplicationUniversalId(innerResult),
                () -> MESSAGE_HELPER.extractSendingApplicationUniversalIdType(innerResult));
    }

    @Override
    public MessageHdDataType getSendingFacilityDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractSendingFacilityNamespace(innerResult),
                () -> MESSAGE_HELPER.extractSendingFacilityUniversalId(innerResult),
                () -> MESSAGE_HELPER.extractSendingFacilityUniversalIdType(innerResult));
    }

    @Override
    public MessageHdDataType getReceivingApplicationDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractReceivingApplicationNamespace(innerResult),
                () -> MESSAGE_HELPER.extractReceivingApplicationUniversalId(innerResult),
                () -> MESSAGE_HELPER.extractReceivingApplicationUniversalIdType(innerResult));
    }

    @Override
    public MessageHdDataType getReceivingFacilityDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractReceivingFacilityNamespace(innerResult),
                () -> MESSAGE_HELPER.extractReceivingFacilityUniversalId(innerResult),
                () -> MESSAGE_HELPER.extractReceivingFacilityUniversalIdType(innerResult));
    }

    protected MessageHdDataType extractMessageHdDataType(
            Supplier<String> namespaceExtractor,
            Supplier<String> universalIdExtractor,
            Supplier<String> universalIdTypeExtractor) {
        String namespace = namespaceExtractor.get();
        String universalId = universalIdExtractor.get();
        String universalIdType = universalIdTypeExtractor.get();

        return new MessageHdDataType(namespace, universalId, universalIdType);
    }
}
