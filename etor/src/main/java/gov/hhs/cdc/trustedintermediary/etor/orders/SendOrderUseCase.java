package gov.hhs.cdc.trustedintermediary.etor.orders;

import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();

    @Inject OrderConverter converter;
    @Inject OrderSender sender;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void convertAndSend(final Order<?> order) throws UnableToSendOrderException {
        var omlOrder = converter.convertMetadataToOmlOrder(order);
        omlOrder = converter.addContactSectionToPatientResource(omlOrder);
        sender.sendOrder(omlOrder);
    }
}
