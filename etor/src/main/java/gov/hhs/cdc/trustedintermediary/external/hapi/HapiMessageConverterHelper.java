package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;

/**
 * Helper class with a variety of utilities to use on a FHIR bundle message. It adds the 'ETOR' tag
 * to a FHIR bundle of type: OML, ORU It also creates the messageHeader resource in a FHIR bundle
 * message.
 */
public class HapiMessageConverterHelper {

    private static final HapiMessageConverterHelper INSTANCE = new HapiMessageConverterHelper();

    public static HapiMessageConverterHelper getInstance() {
        return INSTANCE;
    }

    @Inject Logger logger;

    private HapiMessageConverterHelper() {}

    /**
     * Adds the `ETOR` code to any message provided
     *
     * @param messageBundle the in coming message in a FHIR bundle
     */
    public void addEtorTagToBundle(Bundle messageBundle) {
        var messageHeader = findOrInitializeMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        var systemValue = "http://localcodes.org/ETOR";
        var codeValue = "ETOR";
        var displayValue = "Processed by ETOR";

        if (meta.getTag(systemValue, codeValue) == null) {
            meta.addTag(new Coding(systemValue, codeValue, displayValue));
        }

        messageHeader.setMeta(meta);
    }

    /**
     * Checks if the FHIR bundle has a messageHeader, and it creates one if it is missing
     *
     * @param bundle the in coming message in a FHIR bundle
     * @return returns existing MessageHeader resource or a newly created one
     */
    public static MessageHeader findOrInitializeMessageHeader(Bundle bundle) {
        var messageHeader =
                HapiHelper.resourcesInBundle(bundle, MessageHeader.class).findFirst().orElse(null);
        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }
        return messageHeader;
    }

    /**
     * Finds the first patient in bundle resource or returns null
     *
     * @param bundle
     * @return Patient if found
     */
    public static Patient findPatientOrNull(Bundle bundle) {
        return HapiHelper.resourcesInBundle(bundle, Patient.class).findFirst().orElse(null);
    }

    /**
     * Finds all patient resources inside a given bundle
     *
     * @param bundle Bundle to check
     * @return Stream list of patients.
     */
    public static Stream<Patient> findAllPatients(Bundle bundle) {
        return HapiHelper.resourcesInBundle(bundle, Patient.class);
    }
}
