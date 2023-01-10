package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.etor.order.OrderController;
import gov.hhs.cdc.trustedintermediary.etor.order.OrderMessage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

/**
 * The domain connector for the ETOR domain. It connects it with the larger trusted intermediary.
 */
public class DomainRegistration implements DomainConnector {

    @Inject OrderController orderController;
    @Inject Logger logger;

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(OrderController.class, OrderController.getInstance());
        return Map.of(new HttpEndpoint("POST", "/v1/etor/order"), this::handleOrder);
    }

    @Override
    public String openApiSpecification() {
        try {
            return Files.readString(
                    Paths.get(getClass().getClassLoader().getResource("etor.yaml").toURI()),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    DomainResponse handleOrder(DomainRequest request) {

        logger.logInfo("Parsing request...");
        var order = orderController.parseOrder(request);

        OrderMessage orderMessage = new OrderMessage(order);

        logger.logInfo("Constructing response...");
        return orderController.constructResponse(orderMessage);
    }
}
