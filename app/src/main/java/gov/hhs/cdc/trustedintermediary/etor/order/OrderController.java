package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;

/**
 * Creates an in-memory representation of an order to be ingested by the system, and return response
 * information back to the client.
 */
public class OrderController {

    private static final OrderController ORDER_CONTROLLER = new OrderController();
    @Inject Formatter formatter;
    @Inject Logger logger;

    private OrderController() {}

    public static OrderController getInstance() {
        return ORDER_CONTROLLER;
    }

    public Order parseOrder(DomainRequest request) {
        logger.logInfo("Parsing order...");
        Order order;

        try {
            order = formatter.convertToObject(request.getBody(), Order.class);
        } catch (FormatterProcessingException e) {
            logger.logError("Unable to convert request body to order object");
            throw new RuntimeException(e);
        }
        return order;
    }

    public String constructOrderMessage(Order order) {
        String outputMessage;
        OrderMessage orderMessage = new OrderMessage(order);

        try {
            outputMessage = formatter.convertToString(orderMessage.generateMessage());
        } catch (FormatterProcessingException e) {
            logger.logError("Error constructing order message", e);
            throw new RuntimeException(e);
        }

        return outputMessage;
    }

    public boolean isBodyValid(String body) {

        return (body.isBlank()) ? false : true;
    }
}
