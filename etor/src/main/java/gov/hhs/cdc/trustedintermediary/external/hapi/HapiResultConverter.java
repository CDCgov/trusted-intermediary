package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultConverter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;

/**
 * Converts {@link Result} to a Hapi-specific FHIR lab result {@link Result <Bundle>}) with proper
 * identifying message headers.
 */
public class HapiResultConverter implements ResultConverter {

    private static final HapiResultConverter INSTANCE = new HapiResultConverter();

    @Inject Logger logger;

    public static HapiResultConverter getInstance() {
        return INSTANCE;
    }

    private HapiResultConverter() {}

    @Override
    public Result<?> addEtorProcessingTag(final Result<?> message) {
        var hapiResult = (Result<Bundle>) message;
        var messageBundle = hapiResult.getUnderlyingResult();

        var messageHeaderOptional =
                HapiHelper.resourcesInBundle(messageBundle, MessageHeader.class).findFirst();
        if (messageHeaderOptional.isPresent()) {
            var messageHeader = messageHeaderOptional.get();
            var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

            meta.addTag(new Coding("http://localcodes.org/ETOR", "ETOR", "Processed by ETOR"));
            messageHeader.setMeta(meta);
        } else {
            logger.logInfo("No MessageHeader found in the Bundle to add the ETOR processing tag.");
        }

        return new HapiResult(messageBundle);
    }
}
