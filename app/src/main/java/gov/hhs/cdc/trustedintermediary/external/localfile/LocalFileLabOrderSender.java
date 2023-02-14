package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;

public class LocalFileLabOrderSender implements LabOrderSender {
    @Override
    public void sendOrder(final LabOrder<?> order) {
        // convert the order to JSON?
        // save JSON? to a local file
    }
}
