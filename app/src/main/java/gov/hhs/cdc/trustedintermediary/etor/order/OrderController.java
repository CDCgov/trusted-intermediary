package gov.hhs.cdc.trustedintermediary.etor.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;

/**
 * Creates an in-memory representation of an order to be ingested by the system, and return response
 * information back to the client.
 */
public class OrderController {

    private static final OrderController ORDER_CONTROLLER = new OrderController();

    private Formatter jsonFormatter = ApplicationContext.getImplementation(Formatter.class);
    private final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

    private OrderController() {}

    public static OrderController getInstance() {
        return ORDER_CONTROLLER;
    }

    public Order parseOrder(DomainRequest request) {
        LOGGER.logInfo("Parsing order...");
        Order order = new Order();
        var headers = request.getHeaders();
        var destination = headers.get("Destination");
        var client = headers.get("Client");
        var parsedBody = parseBody(request.getBody());
        order.setDestination(destination);
        order.setBody(parsedBody);
        order.setClient(client);
        return order;
    }

    public String constructOrderMessage(Order order) {
        String outputMessage;

        try {
            outputMessage =
                    jsonFormatter.convertToString(order.generateMessage().getOrderMessage());
        } catch (JsonProcessingException e) {
            LOGGER.logError("Error constructing order message", e);
            throw new RuntimeException(e);
        }

        return outputMessage;
    }

    private String parseBody(String body) {
        // Method to parse the response body,
        // it assumes there is only one key/value pair in the body.
        // This will eventually change depending on the needs.
        LOGGER.logInfo("Parsing body...");
        final int bodyValueIndex = 1;
        if (body.isBlank()) {
            LOGGER.logWarning("Request body is blank, unable to parse body.");
            return body;
        }
        String[] bodyArr = body.split(":");
        if (bodyArr.length < 2) {
            LOGGER.logWarning("Unrecognised structure, unable to parse body");
            return body;
        }
        return bodyArr[bodyValueIndex].substring(1, bodyArr[1].length() - 2);
    }
}
