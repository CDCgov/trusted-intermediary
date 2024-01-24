package gov.hhs.cdc.trustedintermediary.external.hapi;

import org.hl7.fhir.r4.model.Bundle;

import gov.hhs.cdc.trustedintermediary.etor.results.Result;

/**
 * Filler concrete implementation of a {@link Result} using the Hapi FHIR library
 */
public class HapiResult implements Result<Bundle> {

	private final Bundle innerResult;

	public HapiResult(Bundle innerResult) {
		this.innerResult = innerResult;
	}

	@Override
	public Bundle getUnderlyingResult() {
		return null;
	}

	@Override
	public String getFhirResourceId() {
		return null;
	}

	@Override
	public String getPatientId() {
		return null;
	}
}
