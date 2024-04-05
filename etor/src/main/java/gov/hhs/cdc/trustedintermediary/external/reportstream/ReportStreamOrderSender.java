package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Optional;
import javax.inject.Inject;

/** Accepts a {@link Order} and sends it to ReportStream. */
public class ReportStreamOrderSender implements OrderSender {

    private static final ReportStreamOrderSender INSTANCE = new ReportStreamOrderSender();

    @Inject ReportStreamSenderHelper sender;
    @Inject HapiFhir fhir;
    @Inject Logger logger;

    public static ReportStreamOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamOrderSender() {}

    @Override
    public Optional<String> send(final Order<?> order) throws UnableToSendMessageException {
        logger.logInfo("Sending the order to ReportStream");
        String json = fhir.encodeResourceToJson(order.getUnderlyingResource());
        return sender.sendOrderToReportStream(json, order.getFhirResourceId());
    }
}
