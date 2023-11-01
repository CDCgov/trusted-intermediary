package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException;
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.inject.Inject;

/** Accepts a {@link Order} and writes it to a local file. */
public class LocalFileOrderSender implements OrderSender {

    static final String LOCAL_FILE_NAME = "localfileorder.json";
    private static final LocalFileOrderSender INSTANCE = new LocalFileOrderSender();

    @Inject HapiFhir fhir;
    @Inject Logger logger;

    @Inject MetricMetaData metaData;

    public static LocalFileOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileOrderSender() {}

    @Override
    public void sendOrder(final Order<?> order) throws UnableToSendOrderException {
        var fileLocation = Paths.get(LOCAL_FILE_NAME);
        logger.logInfo("Sending the order to the hard drive at {}", fileLocation.toAbsolutePath());

        try {
            metaData.put(order.getFhirResourceId(), MetaDataStep.SENT_TO_PHL);
            String serialized = fhir.encodeResourceToJson(order.getUnderlyingOrder());
            Files.writeString(fileLocation, serialized, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new UnableToSendOrderException("Error writing the lab order", e);
        }
    }
}
