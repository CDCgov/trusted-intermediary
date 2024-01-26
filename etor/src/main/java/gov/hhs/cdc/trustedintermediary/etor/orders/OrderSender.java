package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import java.util.Optional;

/** Interface for sending a lab order. */
public interface OrderSender {
    Optional<String> send(Order<?> order) throws UnableToSendMessageException;
}
