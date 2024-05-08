package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.Collections;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;

/** Helper class with transformation methods that take a FHIR Bundle and modifies it */
public class HapiMessageConverterHelper {

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

    public static void addSendingFacilityToMessageHeader(Bundle bundle, String name) {
        var header =
                HapiHelper.resourcesInBundle(bundle, MessageHeader.class)
                        .findFirst()
                        .orElse(new MessageHeader());
        var org = new Organization().setName(name);
        header.setSender(new Reference(org));
    }

    public static void addReceivingFacilityToMessageHeader(Bundle bundle, String name) {
        var header =
                HapiHelper.resourcesInBundle(bundle, MessageHeader.class)
                        .findFirst()
                        .orElse(new MessageHeader());
        var org = new Organization();
        var destination = new MessageHeader.MessageDestinationComponent();

        org.setName(name);
        destination.setReceiver(new Reference(org));
        header.setDestination(Collections.singletonList(destination));
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(org));
    }
}
