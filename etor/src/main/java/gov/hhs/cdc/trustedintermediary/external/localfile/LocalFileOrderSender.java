package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.metadata.EtorMetadataStep;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetadata;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import javax.inject.Inject;

/** Accepts a {@link Order} and writes it to a local file. */
public class LocalFileOrderSender implements OrderSender {

    static final String LOCAL_FILE_NAME = "localfileorder.json";
    private static final LocalFileOrderSender INSTANCE = new LocalFileOrderSender();

    @Inject HapiFhir fhir;
    @Inject Logger logger;

    @Inject MetricMetadata metadata;

    public static LocalFileOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileOrderSender() {}

    @Override
    public Optional<String> sendOrder(final Order<?> order) throws UnableToSendOrderException {
        var fileLocation = Paths.get(LOCAL_FILE_NAME);
        logger.logInfo("Sending the order to the hard drive at {}", fileLocation.toAbsolutePath());

        try {
            metadata.put(order.getFhirResourceId(), EtorMetadataStep.SENT_TO_REPORT_STREAM);
            String serialized = fhir.encodeResourceToJson(order.getUnderlyingOrder());
            Files.writeString(fileLocation, serialized, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new UnableToSendOrderException("Error writing the lab order", e);
        }

        return Optional.empty();
    }
}
