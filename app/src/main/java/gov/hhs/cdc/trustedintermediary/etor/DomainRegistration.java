package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.etor.order.Order;
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
    @Inject private Logger LOGGER;

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(OrderController.class, OrderController.getInstance());
        return Map.of(new HttpEndpoint("POST", "/v1/etor/order"), this::handleOrder);
    }

    DomainResponse handleOrder(DomainRequest request) {
        var response = new DomainResponse(200);
        Order order = null;

        // TODO request validation
        LOGGER.logInfo("Validating request...");
        var validBody = orderController.isBodyValid(request.getBody());
        if (!validBody) {
            response.setStatusCode(400);
            LOGGER.logWarning("Request body is empty, status code: 400");
        }
        // TODO request processing

        if (validBody) {
            LOGGER.logInfo("Processing request...");
            order = processRequest(request);
        }
        // TODO response, include message

        var orderMessage = generateOrderMessage(order);
        response.setBody(orderMessage);

        return response;
    }

    private String generateOrderMessage(Order order) {
        return orderController.constructOrderMessage(order);
    }

    private Order processRequest(DomainRequest request) {
        return orderController.parseOrder(request);
    }
}
