package gov.hhs.cdc.trustedintermediary.external.localfile;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Accepts a {@link LabOrder} and writes it to a local file. */
public class LocalFileLabOrderSender implements LabOrderSender {
    private static final LocalFileLabOrderSender INSTANCE = new LocalFileLabOrderSender();

    public static LocalFileLabOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {

        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        String serialized =
                parser.encodeResourceToString((IBaseResource) order.getUnderlyingOrder());
        System.out.println(serialized);

        String fileName = "../examples/localfilelaborder.json";

        try {
            Files.writeString(Paths.get(fileName), serialized, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
