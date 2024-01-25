package gov.hhs.cdc.trustedintermediary.etor.results;

public class ResultResponse {
	private String fhirResourceId;

	public ResultResponse(String fhirResourceId) {
		this.fhirResourceId = fhirResourceId;
	}

	public ResultResponse(Result<?> result) {
		this.fhirResourceId = result.getFhirResourceId();
	}

	public String getFhirResourceId() {
		return fhirResourceId;
	}

	public void setFhirResourceId(final String fhirResourceId) {
		this.fhirResourceId = fhirResourceId;
	}
}
