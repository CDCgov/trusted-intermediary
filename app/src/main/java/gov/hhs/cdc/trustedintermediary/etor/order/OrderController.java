package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
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
        String fakeOrderId = "1234abcd";
        String fakeDestination = "fake lab";
        LocalDateTime createAt = LocalDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        String formattedDateTime = createAt.format(dateTimeFormat);
        Order happyOrder = ApplicationContext.getImplementation(Order.class);
        happyOrder.setId(fakeOrderId);
        happyOrder.setDestination(fakeDestination);
        happyOrder.setCreateAt(createAt, dateTimeFormat);

        String outputMessage =
                "order id: "
                        + fakeOrderId
                        + ", "
                        + "destination: "
                        + fakeDestination
                        + ", "
                        + "created at: "
                        + formattedDateTime;

        return outputMessage;
    }
}
