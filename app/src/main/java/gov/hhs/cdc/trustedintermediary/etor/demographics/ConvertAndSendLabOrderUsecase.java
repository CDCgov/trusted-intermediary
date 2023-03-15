package gov.hhs.cdc.trustedintermediary.etor.demographics;

import javax.inject.Inject;

/**
 * The overall logic that handles receiving patient demographics, converting it to a lab order, and
 * sending it on its way.
 */
public class ConvertAndSendLabOrderUsecase {

    private static final ConvertAndSendLabOrderUsecase INSTANCE =
            new ConvertAndSendLabOrderUsecase();

    @Inject LabOrderConverter converter;

    @Inject LabOrderSender sender;

    public static ConvertAndSendLabOrderUsecase getInstance() {
        return INSTANCE;
    }

    private ConvertAndSendLabOrderUsecase() {}

    public void convertAndSend(PatientDemographics demographics) {
        LabOrder<?> labOrder = converter.convertToOrder(demographics);
        sender.sendOrder(labOrder);
    }

    public void convertAndSend2(Demographics<?> demographics) {
        LabOrder<?> labOrder = converter.convertToOrder2(demographics);
        sender.sendOrder(labOrder);
    }
}
