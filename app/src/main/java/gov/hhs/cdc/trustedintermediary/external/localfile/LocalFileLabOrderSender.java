package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.inject.Inject;

/** Accepts a {@link LabOrder} and writes it to a local file. */
public class LocalFileLabOrderSender implements LabOrderSender {

    static final String LOCAL_FILE_NAME = "localfilelaborder.json";
    private static final LocalFileLabOrderSender INSTANCE = new LocalFileLabOrderSender();

    @Inject HapiFhir fhir;
    @Inject Logger logger;

    public static LocalFileLabOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {
        var fileLocation = Paths.get(LOCAL_FILE_NAME);
        logger.logInfo("Sending the order to the hard drive at {}", fileLocation.toAbsolutePath());

        try {
            String serialized = fhir.encodeResourceToJson(order.getUnderlyingOrder());
            Files.writeString(fileLocation, serialized, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.logError("Error writing the lab order", e);
            throw new RuntimeException(e);
        }
    }
}
