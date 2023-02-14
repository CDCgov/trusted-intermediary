package gov.hhs.cdc.trustedintermediary.etor.demographics;

import javax.inject.Inject;

public class ConvertAndSendLabOrderUsecase {

    @Inject LabOrderConverter converter;

    @Inject LabOrderSender sender;

    public void convertAndSend(PatientDemographics demographics) {
        LabOrder<?> labOrder = converter.convertToOrder(demographics);
        sender.sendOrder(labOrder);
    }
}
