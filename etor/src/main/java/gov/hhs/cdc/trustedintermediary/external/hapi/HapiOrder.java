package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import java.util.function.Supplier;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

/**
 * A concrete implementation of a {@link Order} that uses the Hapi FHIR bundle as its underlying
 * type.
 */
public class HapiOrder implements Order<Bundle> {

    private final HapiMessageHelper MESSAGE_HELPER =
            ApplicationContext.getImplementation(HapiMessageHelper.class);

    private final Bundle innerOrder;

    public HapiOrder(Bundle innerOrder) {
        this.innerOrder = innerOrder;
    }

    @Override
    public Bundle getUnderlyingElement() {
        return innerOrder;
    }

    @Override
    public String getFhirResourceId() {
        return innerOrder.getId();
    }

    @Override
    public String getPatientId() {
        return HapiHelper.resourcesInBundle(innerOrder, Patient.class)
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getPlacerOrderNumber() {
        return MESSAGE_HELPER.extractPlacerOrderNumber(innerOrder);
    }

    @Override
    public MessageHdDataType getSendingApplicationDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractSendingApplicationNamespace(innerOrder),
                () -> MESSAGE_HELPER.extractSendingApplicationUniversalId(innerOrder),
                () -> MESSAGE_HELPER.extractSendingApplicationUniversalIdType(innerOrder));
    }

    @Override
    public MessageHdDataType getSendingFacilityDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractSendingFacilityNamespace(innerOrder),
                () -> MESSAGE_HELPER.extractSendingFacilityUniversalId(innerOrder),
                () -> MESSAGE_HELPER.extractSendingFacilityUniversalIdType(innerOrder));
    }

    @Override
    public MessageHdDataType getReceivingApplicationDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractReceivingApplicationNamespace(innerOrder),
                () -> MESSAGE_HELPER.extractReceivingApplicationUniversalId(innerOrder),
                () -> MESSAGE_HELPER.extractReceivingApplicationUniversalIdType(innerOrder));
    }

    @Override
    public MessageHdDataType getReceivingFacilityDetails() {
        return extractMessageHdDataType(
                () -> MESSAGE_HELPER.extractReceivingFacilityNamespace(innerOrder),
                () -> MESSAGE_HELPER.extractReceivingFacilityUniversalId(innerOrder),
                () -> MESSAGE_HELPER.extractReceivingFacilityUniversalIdType(innerOrder));
    }

    private MessageHdDataType extractMessageHdDataType(
            Supplier<String> namespaceExtractor,
            Supplier<String> universalIdExtractor,
            Supplier<String> universalIdTypeExtractor) {

        String namespace = namespaceExtractor.get();
        String universalId = universalIdExtractor.get();
        String universalIdType = universalIdTypeExtractor.get();

        return new MessageHdDataType(namespace, universalId, universalIdType);
    }
}
