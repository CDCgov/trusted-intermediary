package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultConverter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Converts {@link Result} to a Hapi-specific FHIR lab result {@link Result <Bundle>}) with proper
 * identifying message headers.
 */
public class HapiResultConverter implements ResultConverter {

    private static final HapiResultConverter INSTANCE = new HapiResultConverter();

	@Inject
	HapiMessageConverterHelper hapiMessageConverterHelper;

	@Inject
	Logger logger;

    public static HapiResultConverter getInstance() {
        return INSTANCE;
    }

    private HapiResultConverter() {}

    @Override
    public Result<?> addEtorProcessingTag(final Result<?> message) {
        var hapiResult = (Result<Bundle>) message;
        var messageBundle = hapiResult.getUnderlyingResult();

		hapiMessageConverterHelper.addEtorTag(messageBundle);

        return new HapiResult(messageBundle);
    }
}
