package gov.hhs.cdc.trustedintermediary.etor.results;

public class ResultResponse {
	private String fhirResourceId;
	private String patientId;

	public ResultResponse(String fhirResourceId, String patientId) {
		this.fhirResourceId = fhirResourceId;
		this.patientId = patientId;
	}

	public ResultResponse(Result<?> result) {
		this.fhirResourceId = result.getFhirResourceId();
		this.patientId = result.getPatientId();
	}

	public String getFhirResourceId() {
		return fhirResourceId;
	}

	public void setFhirResourceId(final String fhirResourceId) {
		this.fhirResourceId = fhirResourceId;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(final String patientId) {
		this.patientId = patientId;
	}
}
