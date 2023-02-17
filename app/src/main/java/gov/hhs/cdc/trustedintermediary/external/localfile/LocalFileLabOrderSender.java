package gov.hhs.cdc.trustedintermediary.external.localfile;

import ca.uhn.fhir.context.FhirContext;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;

/** Accepts a {@link LabOrder} and writes it to a local file. */
public class LocalFileLabOrderSender implements LabOrderSender {
    private static final LocalFileLabOrderSender INSTANCE = new LocalFileLabOrderSender();

    public static LocalFileLabOrderSender getInstance() {
        return INSTANCE;
    }

    private LocalFileLabOrderSender() {}

    @Override
    public void sendOrder(final LabOrder<?> order) {

        //        // Create a FHIR context
        FhirContext ctx = FhirContext.forR4();
        //
        //        // Create a Patient resource to serialize
        //        Patient patient = new Patient();
        //        patient.addName().setFamily("Simpson").addGiven("James");
        //
        //        // Instantiate a new JSON parser
        //        IParser parser = ctx.newJsonParser();
        //
        //        // Serialize it
        //        String serialized = parser.encodeResourceToString(patient);
        //        System.out.println(serialized);
        System.out.println("THIS IS THE STRING: " + order.getUnderlyingOrder().toString());
        //        String fileName = "/xyz/test.txt";
        //        String messageToWrite = "My long string";
        //        Files.writeString(Paths.get(fileName), messageToWrite,
        // StandardCharsets.ISO_8859_1);

        // convert the order to JSON?
        // save JSON? to a local file
    }
}
