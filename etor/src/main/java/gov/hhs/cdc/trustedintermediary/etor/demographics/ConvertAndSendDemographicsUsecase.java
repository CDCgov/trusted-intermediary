package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageSender;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageUseCase;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter;
import javax.inject.Inject;

/**
 * The overall logic that handles receiving patient demographics, converting it to a lab order, and
 * sending it on its way.
 */
public class ConvertAndSendDemographicsUsecase implements SendMessageUseCase<Demographics<?>> {

    private static final ConvertAndSendDemographicsUsecase INSTANCE =
            new ConvertAndSendDemographicsUsecase();

    @Inject OrderConverter converter;

    @Inject MessageSender<Order<?>> sender;

    public static ConvertAndSendDemographicsUsecase getInstance() {
        return INSTANCE;
    }

    private ConvertAndSendDemographicsUsecase() {}

    public void convertAndSend(Demographics<?> demographics) throws UnableToSendMessageException {
        Order<?> order = converter.convertToOrder(demographics);
        sender.send(order);
    }
}
