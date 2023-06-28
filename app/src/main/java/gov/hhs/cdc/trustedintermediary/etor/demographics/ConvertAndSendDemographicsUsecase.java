package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.orders.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendLabOrderException;
import javax.inject.Inject;

/**
 * The overall logic that handles receiving patient demographics, converting it to a lab order, and
 * sending it on its way.
 */
public class ConvertAndSendDemographicsUsecase {

    private static final ConvertAndSendDemographicsUsecase INSTANCE =
            new ConvertAndSendDemographicsUsecase();

    @Inject LabOrderConverter converter;

    @Inject LabOrderSender sender;

    public static ConvertAndSendDemographicsUsecase getInstance() {
        return INSTANCE;
    }

    private ConvertAndSendDemographicsUsecase() {}

    public void convertAndSend(Demographics<?> demographics) throws UnableToSendLabOrderException {
        LabOrder<?> labOrder = converter.convertToOrder(demographics);
        sender.sendOrder(labOrder);
    }
}
