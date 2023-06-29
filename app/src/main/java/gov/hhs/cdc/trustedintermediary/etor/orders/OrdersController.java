package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper;
import javax.inject.Inject;

/** Responsible for constructing a response for orders requests to be returned to the client. */
public class OrdersController {

    private static final OrdersController INSTANCE = new OrdersController();

    @Inject DomainResponseHelper domainResponseHelper;

    private OrdersController() {}

    public static OrdersController getInstance() {
        return INSTANCE;
    }

    public DomainResponse constructResponse(OrdersResponse ordersResponse) {
        return domainResponseHelper.constructOkResponse(ordersResponse);
    }
}
