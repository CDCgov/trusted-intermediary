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
    public Bundle getUnderlyingOrder() {
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
    public String getSendingApplicationDetails() {
        MessageHdDataType sendingApplicationDetails =
                MESSAGE_HELPER.extractSendingApplicationDetails(innerOrder);
        return sendingApplicationDetails.toString();
    }

    @Override
    public String getSendingFacilityDetails() {
        MessageHdDataType sendingFacilityDetails =
                MESSAGE_HELPER.extractSendingFacilityDetails(innerOrder);
        return sendingFacilityDetails.toString();
    }

    @Override
    public String getReceivingApplicationDetails() {
        MessageHdDataType receivingApplicationDetails =
                MESSAGE_HELPER.extractReceivingApplicationDetails(innerOrder);
        return receivingApplicationDetails.toString();
    }

    @Override
    public String getReceivingFacilityDetails() {
        MessageHdDataType receivingFacilityDetails =
                MESSAGE_HELPER.extractReceivingFacilityDetails(innerOrder);
        return receivingFacilityDetails.toString();
    }
}
