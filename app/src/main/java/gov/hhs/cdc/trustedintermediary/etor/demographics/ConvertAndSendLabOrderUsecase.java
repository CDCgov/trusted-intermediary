package gov.hhs.cdc.trustedintermediary.etor.demographics;

import javax.inject.Inject;

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
}
