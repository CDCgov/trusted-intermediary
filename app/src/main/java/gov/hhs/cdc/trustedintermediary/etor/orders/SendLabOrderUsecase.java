package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException;
import javax.inject.Inject;

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
