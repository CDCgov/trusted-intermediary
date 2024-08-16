package gov.hhs.cdc.trustedintermediary.etor.results;

public record ResultResponse(String fhirResourceId) {

    public ResultResponse(Result<?> result) {
        this(result.getFhirResourceId());
    }
}
