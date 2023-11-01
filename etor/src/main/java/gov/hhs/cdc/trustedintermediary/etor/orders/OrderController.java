package gov.hhs.cdc.trustedintermediary.etor.orders;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrder;
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

/** Responsible for constructing a response for orders requests to be returned to the client. */
public class OrderController {

    private static final OrderController INSTANCE = new OrderController();

    @Inject HapiFhir fhir;
    @Inject Logger logger;
    @Inject MetricMetaData metaData;

    private OrderController() {}

    public static OrderController getInstance() {
        return INSTANCE;
    }

    public Order<?> parseOrders(DomainRequest request) throws FhirParseException {
        logger.logInfo("Parsing orders");
        var fhirBundle = fhir.parseResource(request.getBody(), Bundle.class);
        metaData.put(fhirBundle.getId(), MetaDataStep.RECEIVED_FROM_REPORT_STREAM);
        return new HapiOrder(fhirBundle);
    }
}
