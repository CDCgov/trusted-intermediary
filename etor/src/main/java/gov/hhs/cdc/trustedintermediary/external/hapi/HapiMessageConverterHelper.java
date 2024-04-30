package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;

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

    public static void addEtorTagToBundle(Bundle messageBundle) {
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

    public static MessageHeader findOrInitializeMessageHeader(Bundle bundle) {
        var messageHeader =
                HapiHelper.resourcesInBundle(bundle, MessageHeader.class).findFirst().orElse(null);
        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }
        return messageHeader;
    }
}
