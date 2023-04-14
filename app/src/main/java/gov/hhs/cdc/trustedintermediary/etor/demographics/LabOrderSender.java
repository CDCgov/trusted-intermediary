package gov.hhs.cdc.trustedintermediary.etor.demographics;

import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/** Interface for sending a lab order. */
public interface LabOrderSender {
    void sendOrder(LabOrder<?> order)
            throws UnableToSendLabOrderException, SecretRetrievalException, InvalidKeySpecException,
                    NoSuchAlgorithmException;
}
