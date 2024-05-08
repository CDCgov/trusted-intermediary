package gov.hhs.cdc.trustedintermediary.etor.orders;

/** Interface for converting things to orders and things in orders. */
public interface OrderConverter {
    Order<?> convertToOmlOrder(Order<?> order);

    Order<?> addContactSectionToPatientResource(Order<?> order);

    Order<?> addEtorProcessingTag(Order<?> message);
}
