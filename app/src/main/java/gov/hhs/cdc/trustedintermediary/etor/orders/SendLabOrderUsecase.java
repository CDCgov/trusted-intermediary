package gov.hhs.cdc.trustedintermediary.etor.orders;

import javax.inject.Inject;

/** The overall logic to receive and subsequently send a lab order. */
public class SendLabOrderUsecase {
    private static final SendLabOrderUsecase INSTANCE = new SendLabOrderUsecase();

    @Inject LabOrderSender sender;

    private SendLabOrderUsecase() {}

    public static SendLabOrderUsecase getInstance() {
        return INSTANCE;
    }

    public void send(LabOrder<?> order) throws UnableToSendLabOrderException {
        sender.sendOrder(order);
    }
}
