package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Accepts a {@link LabOrder} and writes it to a local file. */
public class LocalFileLabOrderSender implements LabOrderSender {
    private static final LocalFileLabOrderSender INSTANCE = new LocalFileLabOrderSender();

    @Inject HapiFhir fhir;

    public static LocalFileLabOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {

        String serialized = fhir.encodeResourceToJson((IBaseResource) order.getUnderlyingOrder());

        String fileName = "../examples/localfilelaborder.json";

        try {
            Files.writeString(Paths.get(fileName), serialized, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
