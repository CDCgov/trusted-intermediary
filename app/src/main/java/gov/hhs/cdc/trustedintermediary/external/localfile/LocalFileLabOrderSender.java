package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;

public class LocalFileLabOrderSender implements LabOrderSender {
    private static final LocalFileLabOrderSender INSTANCE = new LocalFileLabOrderSender();

    public static LocalFileLabOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {
        // convert the order to JSON?
        // save JSON? to a local file
    }
}
