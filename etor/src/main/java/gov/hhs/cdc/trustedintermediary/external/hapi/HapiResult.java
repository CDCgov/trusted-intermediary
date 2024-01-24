package gov.hhs.cdc.trustedintermediary.external.hapi;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

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
		return innerResult;
	}

	@Override
	public String getFhirResourceId() {
		return innerResult.getId();
	}

	@Override
	public String getPatientId() {
		return HapiHelper.resourcesInBundle(innerResult, Patient.class)
			.flatMap(patient -> patient.getIdentifier().stream())
			.filter(
				identifier ->
					identifier
						.getType()
						.hasCoding(
							"http://terminology.hl7.org/CodeSystem/v2-0203",
							"MR"))
			.map(Identifier::getValue)
			.findFirst()
			.orElse("");
	}
}
