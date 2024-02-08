package gov.hhs.cdc.trustedintermediary.etor.results;

public interface ResultConverter {
	Result<?> addEtorProcessingTag(Result<?> message);
}
