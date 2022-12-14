package gov.hhs.cdc.trustedintermediary.etor.order;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class OrderController {

    private static final OrderController ORDER_CONTROLLER = new OrderController();

    private OrderController() {}

    public static OrderController getInstance() {
        return ORDER_CONTROLLER;
    }

    public String parseOrder(String requestBody) {
        return "DogCow sent in a lab order";
    }

    // TODO assemble json message
    public String constructOrderMessage() {
        // get information from an order object (will be created in the future)
        String orderId = "1234abcd";
        String destination = "fake lab";
        LocalDateTime createAt = LocalDateTime.now(ZoneId.of("UTC"));

        String outputMessage =
                "order id: "
                        + orderId
                        + ", "
                        + "destination: "
                        + destination
                        + ", "
                        + "created at: "
                        + createAt;

        return "";
    }
}
