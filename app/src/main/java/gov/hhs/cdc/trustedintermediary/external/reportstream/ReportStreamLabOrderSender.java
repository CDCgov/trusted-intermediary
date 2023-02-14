package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;

public class ReportStreamLabOrderSender implements LabOrderSender {
    @Override
    public void sendOrder(final LabOrder<?> order) {
        // String json = FHIR.stringify(order.getUnderlyingOrder())
        // reportStream.sendRequestBody(json)
    }
}
