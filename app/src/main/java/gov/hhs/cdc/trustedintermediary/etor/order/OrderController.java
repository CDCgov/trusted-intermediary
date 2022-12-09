package gov.hhs.cdc.trustedintermediary.etor.order;

public class OrderController {

    private static final OrderController ORDER_CONTROLLER = new OrderController();

    private OrderController() {}

    public static OrderController getInstance() {
        return ORDER_CONTROLLER;
    }

    public String parseOrder(String requestBody) {
        return "DogCow sent in a lab order FAILED";
    }
}
