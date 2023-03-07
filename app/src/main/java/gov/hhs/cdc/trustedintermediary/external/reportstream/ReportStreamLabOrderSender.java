package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;

/** Accepts a {@link LabOrder} and sends it to ReportStream. */
public class ReportStreamLabOrderSender implements LabOrderSender {

    // @Inject ReportStreamConnection reportStream;

    private static final ReportStreamLabOrderSender INSTANCE = new ReportStreamLabOrderSender();

    public static ReportStreamLabOrderSender getInstance() {
        return INSTANCE;
    }

    private ReportStreamLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {
        // String json = FHIR.stringify(order.getUnderlyingOrder())
        // decide if the environment is in prod or local
        // String bearerToken = reportStream.requestToken()
        // reportStream.sendRequestBody(url, json, bearerToken)
    }
}
