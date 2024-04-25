package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.Message;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

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

    public String getPatientId() {
        return HapiHelper.resourcesInBundle(innerResource, Patient.class)
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                "MR".equals(identifier.getType().getCodingFirstRep().getCode()))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
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
