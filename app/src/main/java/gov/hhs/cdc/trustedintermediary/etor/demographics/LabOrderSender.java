package gov.hhs.cdc.trustedintermediary.etor.demographics;

public interface LabOrderSender {
    void sendOrder(LabOrder<?> order);
}
