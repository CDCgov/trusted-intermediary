package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Interface for sending a lab order. */
public interface OrderSender {
    Optional<String> sendOrder(Order<?> order) throws UnableToSendOrderException;
}
