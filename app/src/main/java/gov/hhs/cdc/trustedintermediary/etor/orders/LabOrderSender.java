package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Interface for sending a lab order. */
public interface LabOrderSender {
    void sendOrder(LabOrder<?> order) throws UnableToSendLabOrderException;
}
