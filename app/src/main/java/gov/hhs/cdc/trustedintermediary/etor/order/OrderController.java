package gov.hhs.cdc.trustedintermediary.etor.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;

public class OrderController {

    private static final OrderController ORDER_CONTROLLER = new OrderController();
    private Order order = ApplicationContext.getImplementation(Order.class);

    private OrderController() {}

    public static OrderController getInstance() {
        return ORDER_CONTROLLER;
    }

    public String parseOrder(String requestBody) {
        // order.setBody(requestBody);
        return "DogCow just sent a lab order!";
    }

    public String constructOrderMessage(Order order) {
        Formatter jsonFormatter = ApplicationContext.getImplementation(Formatter.class);
        String outputMessage;

        try {
            outputMessage =
                    jsonFormatter.convertToString(order.generateMessage().getOrderMessage());
        } catch (JsonProcessingException e) {
            // Logger should catch this error
            throw new RuntimeException(e);
        }

        return outputMessage;
    }
}
