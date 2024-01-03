package gov.hhs.cdc.trustedintermediary.etor.orders;

import java.util.Optional;

/** Interface for sending a lab order. */
public interface OrderSender {
    Optional<String> sendOrder(Order<?> order) throws UnableToSendOrderException;
}
