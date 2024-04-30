package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine;
import javax.inject.Inject;

/**
 * The overall logic that handles receiving patient demographics, converting it to a lab order, and
 * sending it on its way.
 */
public class ConvertAndSendDemographicsUsecase {

    private static final ConvertAndSendDemographicsUsecase INSTANCE =
            new ConvertAndSendDemographicsUsecase();

    @Inject TransformationRuleEngine transformationEngine;

    @Inject OrderSender sender;

    public static ConvertAndSendDemographicsUsecase getInstance() {
        return INSTANCE;
    }

    private ConvertAndSendDemographicsUsecase() {}

    public void convertAndSend(Demographics<?> demographics) throws UnableToSendMessageException {
        transformationEngine.runRules(demographics);
        sender.send((Order<?>) demographics);
    }
}
