package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.TransformationRuleEngine;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrder;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

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
        var order = new HapiOrder((Bundle) demographics.getUnderlyingResource());
        sender.send(order);
    }
}
