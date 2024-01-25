package gov.hhs.cdc.trustedintermediary.external.hapi;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import gov.hhs.cdc.trustedintermediary.etor.messages.Message;

public class HapiMessage implements Message<Bundle> {
	private final Bundle innerMessage;

	public HapiMessage(Bundle innerMessage) {
		this.innerMessage = innerMessage;
	}

	@Override
	public Bundle getUnderlyingMessage() {
		return innerMessage;
	}

	@Override
	public String getFhirResourceId() {
		return innerMessage.getId();
	}

	@Override
	public String getPatientId() {
		return HapiHelper.resourcesInBundle(innerMessage, Patient.class)
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
