package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.Message;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Bundle;

public class HapiMessage implements Message<Bundle> {

    protected final HapiMessageHelper MESSAGE_HELPER =
            ApplicationContext.getImplementation(HapiMessageHelper.class);

    protected final Bundle innerResource;

    public HapiMessage(Bundle innerResource) {
        this.innerResource = innerResource;
    }

    @Override
    public Bundle getUnderlyingResource() {
        return innerResource;
    }

    @Override
    public String getFhirResourceId() {
        return innerResource.getId();
    }

    @Override
    public String getPlacerOrderNumber() {
        return MESSAGE_HELPER.extractPlacerOrderNumber(innerResource);
    }

    @Override
    public MessageHdDataType getSendingApplicationDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractSendingApplicationNamespace(innerResource),
                () -> MESSAGE_HELPER.extractSendingApplicationUniversalId(innerResource),
                () -> MESSAGE_HELPER.extractSendingApplicationUniversalIdType(innerResource));
    }

    @Override
    public MessageHdDataType getSendingFacilityDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractSendingFacilityNamespace(innerResource),
                () -> MESSAGE_HELPER.extractSendingFacilityUniversalId(innerResource),
                () -> MESSAGE_HELPER.extractSendingFacilityUniversalIdType(innerResource));
    }

    @Override
    public MessageHdDataType getReceivingApplicationDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractReceivingApplicationNamespace(innerResource),
                () -> MESSAGE_HELPER.extractReceivingApplicationUniversalId(innerResource),
                () -> MESSAGE_HELPER.extractReceivingApplicationUniversalIdType(innerResource));
    }

    @Override
    public MessageHdDataType getReceivingFacilityDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractReceivingFacilityNamespace(innerResource),
                () -> MESSAGE_HELPER.extractReceivingFacilityUniversalId(innerResource),
                () -> MESSAGE_HELPER.extractReceivingFacilityUniversalIdType(innerResource));
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
