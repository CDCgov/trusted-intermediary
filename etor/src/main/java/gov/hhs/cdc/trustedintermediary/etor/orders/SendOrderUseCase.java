package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();
    @Inject OrderConverter converter;
    @Inject OrderSender sender;
    @Inject MetricMetaData metaData;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void convertAndSend(final Order<?> order) throws UnableToSendOrderException {
        var omlOrder = converter.convertMetadataToOmlOrder(order);
        metaData.put(order.getFhirResourceId(), MetaDataStep.ORDER_CONVERTED_TO_OML);
        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        metaData.put(order.getFhirResourceId(), MetaDataStep.CONTACT_SECTION_ADDED_TO_PATIENT);
        sender.sendOrder(omlOrder);
    }
}
