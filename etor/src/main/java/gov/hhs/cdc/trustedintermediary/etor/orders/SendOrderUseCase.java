package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetaDataStep;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import java.time.Instant;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    @Inject OrderConverter converter;
    @Inject OrderSender sender;
    @Inject MetricMetaData metaData;
    @Inject PartnerMetadataStorage partnerMetadataStorage;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void convertAndSend(final Order<?> order) throws UnableToSendOrderException {
        var partnerMetadata =
                new PartnerMetadata(
                        "uniqueId", "senderName", "receiverName", Instant.now(), "abcd");
        partnerMetadataStorage.saveMetadata(partnerMetadata);

        var omlOrder = converter.convertMetadataToOmlOrder(order);
        metaData.put(order.getFhirResourceId(), EtorMetaDataStep.ORDER_CONVERTED_TO_OML);
        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        metaData.put(order.getFhirResourceId(), EtorMetaDataStep.CONTACT_SECTION_ADDED_TO_PATIENT);
        sender.sendOrder(omlOrder);
    }
}
