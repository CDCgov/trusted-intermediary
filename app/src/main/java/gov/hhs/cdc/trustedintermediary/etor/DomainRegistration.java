package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.etor.order.OrderController;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

/**
 * The domain connector for the ETOR domain. It connects it with the larger trusted intermediary.
 */
public class DomainRegistration implements DomainConnector {

    @Inject OrderController orderController;
    private final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(OrderController.class, OrderController.getInstance());
        return Map.of(new HttpEndpoint("POST", "/v1/etor/order"), this::handleOrder);
    }

    DomainResponse handleOrder(DomainRequest request) {
        var response = new DomainResponse(200);

        // TODO request validation
        LOGGER.logInfo("Validating request...");
        // TODO request processing
        LOGGER.logInfo("Processing request");
        // TODO response, include message

        var order = orderController.parseOrder(request);
        var orderMessage = orderController.constructOrderMessage(order);
        response.setBody(orderMessage);

        return response;
    }
}
