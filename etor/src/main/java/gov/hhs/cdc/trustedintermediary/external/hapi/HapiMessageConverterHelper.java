package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;

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
     * @param messageBundle
     */
    public void addEtorTag(Bundle messageBundle) {
        var messageHeaderOptional =
                HapiHelper.resourcesInBundle(messageBundle, MessageHeader.class).findFirst();
        var messageHeader =
                messageHeaderOptional.isPresent()
                        ? messageHeaderOptional.get()
                        : new MessageHeader();
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        meta.addTag(new Coding("http://localcodes.org/ETOR", "ETOR", "Processed by ETOR"));
        messageHeader.setMeta(meta);
    }
}
