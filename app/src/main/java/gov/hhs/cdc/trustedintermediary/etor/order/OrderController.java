package gov.hhs.cdc.trustedintermediary.etor.order;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        String formattedDateTime = createAt.format(dateTimeFormat);

        String outputMessage =
                "order id: "
                        + orderId
                        + ", "
                        + "destination: "
                        + destination
                        + ", "
                        + "created at: "
                        + formattedDateTime;

        return outputMessage;
    }
}
