package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.tasks.Task;
import gov.hhs.cdc.trustedintermediary.etor.tasks.TaskNotifier;
import javax.inject.Inject;

/** The overall logic to receive, convert to OML, and subsequently send a lab order. */
public class SendOrderUseCase {
    private static final SendOrderUseCase INSTANCE = new SendOrderUseCase();

    @Inject OrderConverter converter;
    @Inject OrderSender sender;
    @Inject TaskNotifier notifier;

    private SendOrderUseCase() {}

    public static SendOrderUseCase getInstance() {
        return INSTANCE;
    }

    public void send(final Order<?> order) throws UnableToSendOrderException {
        var omlOrder = converter.convertMetadataToOmlOrder(order);
        sender.sendOrder(omlOrder);
    }

    public void sendWithoutModification(final Order<?> order) throws UnableToSendOrderException {
        sender.sendOrder(order);
    }

    public void sendTask(final Task<?> task) throws UnableToSendOrderException {
        // TODO: save the task, we now own it.
        // TODO: grab ServiceRequest, from the OG EHR, modify the `intent` and save it for when the
        // lab requests it.
        notifier.sendTaskId(task);
    }
}
