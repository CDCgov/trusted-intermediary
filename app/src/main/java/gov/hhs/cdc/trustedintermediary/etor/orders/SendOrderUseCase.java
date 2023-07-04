package gov.hhs.cdc.trustedintermediary.etor.orders;

import javax.inject.Inject;

/** The overall logic to receive and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();

    @Inject OrderSender sender;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void send(Order<?> order) throws UnableToSendOrderException {
        sender.sendOrder(order);
    }
}
