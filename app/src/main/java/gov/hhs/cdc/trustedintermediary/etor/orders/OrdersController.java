package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiLabOrder;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

/** Responsible for constructing a response for orders requests to be returned to the client. */
public class OrdersController {

    private static final OrdersController INSTANCE = new OrdersController();

    @Inject HapiFhir fhir;
    @Inject Logger logger;

    private OrdersController() {}

    public static OrdersController getInstance() {
        return INSTANCE;
    }

    public LabOrder<?> parseOrders(DomainRequest request) {
        logger.logInfo("Parsing orders");
        var fhirBundle = fhir.parseResource(request.getBody(), Bundle.class);
        return new HapiLabOrder(fhirBundle);
    }
}
