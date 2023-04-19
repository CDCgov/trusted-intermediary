package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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

    public void convertAndSend(Demographics<?> demographics)
            throws UnableToSendLabOrderException, SecretRetrievalException, InvalidKeySpecException,
                    NoSuchAlgorithmException {
        LabOrder<?> labOrder = converter.convertToOrder(demographics);
        sender.sendOrder(labOrder);
    }
}
