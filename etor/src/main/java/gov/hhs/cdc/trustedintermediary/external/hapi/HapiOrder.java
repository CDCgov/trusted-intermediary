package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
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
        String sendingApplicationNamespace =
                MESSAGE_HELPER.extractSendingApplicationNamespace(innerOrder);
        String sendingApplicationUniversalId =
                MESSAGE_HELPER.extractSendingApplicationUniversalId(innerOrder);
        String sendingApplicationUniversalIdType =
                MESSAGE_HELPER.extractSendingApplicationUniversalIdType(innerOrder);

        return new MessageHdDataType(
                sendingApplicationNamespace,
                sendingApplicationUniversalId,
                sendingApplicationUniversalIdType);
    }

    @Override
    public MessageHdDataType getSendingFacilityDetails() {
        String sendingFacilityNamespace =
                MESSAGE_HELPER.extractSendingFacilityNamespace(innerOrder);
        String sendingFacilityUniversalId =
                MESSAGE_HELPER.extractSendingFacilityUniversalId(innerOrder);
        String sendingFacilityUniversalIdType =
                MESSAGE_HELPER.extractSendingFacilityUniversalIdType(innerOrder);

        return new MessageHdDataType(
                sendingFacilityNamespace,
                sendingFacilityUniversalId,
                sendingFacilityUniversalIdType);
    }

    @Override
    public MessageHdDataType getReceivingApplicationDetails() {
        String receivingApplicationNamespace =
                MESSAGE_HELPER.extractReceivingApplicationNamespace(innerOrder);
        String receivingApplicationUniversalId =
                MESSAGE_HELPER.extractReceivingApplicationUniversalId(innerOrder);
        String receivingApplicationUniversalIdType =
                MESSAGE_HELPER.extractReceivingApplicationUniversalIdType(innerOrder);

        return new MessageHdDataType(
                receivingApplicationNamespace,
                receivingApplicationUniversalId,
                receivingApplicationUniversalIdType);
    }

    @Override
    public MessageHdDataType getReceivingFacilityDetails() {
        String receivingFacilityNamespace =
                MESSAGE_HELPER.extractReceivingFacilityNamespace(innerOrder);
        String receivingFacilityUniversalId =
                MESSAGE_HELPER.extractReceivingFacilityUniversalId(innerOrder);
        String receivingFacilityUniversalIdType =
                MESSAGE_HELPER.extractReceivingFacilityUniversalIdType(innerOrder);

        return new MessageHdDataType(
                receivingFacilityNamespace,
                receivingFacilityUniversalId,
                receivingFacilityUniversalIdType);
    }
}
