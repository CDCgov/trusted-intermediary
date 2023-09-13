package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Interface for sending a lab order. */
public interface OrderSender {
    void sendOrder(Order<?> order) throws UnableToSendOrderException;
}
