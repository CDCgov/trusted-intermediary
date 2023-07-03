package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException;
import javax.inject.Inject;

/**
 * The overall logic that handles receiving patient demographics, converting it to a lab order, and
 * sending it on its way.
 */
public class ConvertAndSendDemographicsUsecase {

    private static final ConvertAndSendDemographicsUsecase INSTANCE =
            new ConvertAndSendDemographicsUsecase();

    @Inject OrderConverter converter;

    @Inject OrderSender sender;

    public static ConvertAndSendDemographicsUsecase getInstance() {
        return INSTANCE;
    }

    private ConvertAndSendDemographicsUsecase() {}

    public void convertAndSend(Demographics<?> demographics) throws UnableToSendOrderException {
        Order<?> order = converter.convertToOrder(demographics);
        sender.sendOrder(order);
    }
}
