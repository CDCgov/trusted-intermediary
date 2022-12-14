package gov.hhs.cdc.trustedintermediary.etor.order;

/**
 * Creates an in-memory representation of an order to be ingested by the system,
 * and return response information back to the client.
 */
public class OrderController {

    private static final OrderController ORDER_CONTROLLER = new OrderController();

    private OrderController() {}

    public static OrderController getInstance() {
        return ORDER_CONTROLLER;
    }

    public String parseOrder(String requestBody) {
        return "DogCow sent in a lab order";
    }
}
