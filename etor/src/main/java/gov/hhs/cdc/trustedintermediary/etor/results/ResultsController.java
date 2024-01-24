package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiResult;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
public class ResultsController {

	private static final ResultsController INSTANCE = new ResultsController();
	@Inject HapiFhir fhir;
	@Inject Logger logger;
	private ResultsController() {}

	public static ResultsController getInstance() {
		return INSTANCE;
	}

	public Result<?> parseOrders(DomainRequest request) throws FhirParseException {
		logger.logInfo("Parsing results");
		var fhirBundle = fhir.parseResource(request.getBody(), Bundle.class);
		// ETOR Results metadata
		return new HapiResult(fhirBundle);
	}
}
