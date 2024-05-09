package gov.hhs.cdc.trustedintermediary.external.hapi;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;

/** Helper class with transformation methods that take a FHIR Bundle and modifies it */
public class HapiMessageConverterHelper {

    private HapiMessageConverterHelper() {}

    public static void addMetaTag(
            Bundle messageBundle, String system, String code, String display) {
        var messageHeader = findOrInitializeMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        if (meta.getTag(system, code) == null) {
            meta.addTag(new Coding(system, code, display));
        }

        messageHeader.setMeta(meta);
    }

    public static void setMessageTypeCoding(Bundle order, Coding coding) {
        var messageHeader = HapiMessageConverterHelper.findOrInitializeMessageHeader(order);
        messageHeader.setEvent(coding);
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
