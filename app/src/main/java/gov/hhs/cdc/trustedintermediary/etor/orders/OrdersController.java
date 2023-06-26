package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import javax.inject.Inject;

public class OrdersController {

    private static final OrdersController INSTANCE = new OrdersController();

    @Inject Formatter formatter;
    @Inject Logger logger;
    @Inject DomainResponseHelper domainResponseHelper;

    private OrdersController() {}

    public static OrdersController getInstance() {
        return INSTANCE;
    }

    public DomainResponse constructResponse(OrdersResponse ordersResponse) {
        return domainResponseHelper.constructResponse(ordersResponse);
    }
}
