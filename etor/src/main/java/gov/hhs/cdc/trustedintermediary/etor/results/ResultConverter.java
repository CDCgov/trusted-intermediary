package gov.hhs.cdc.trustedintermediary.etor.results;

/** Interface for converting things to results and things in results. */
public interface ResultConverter {
    Result<?> addEtorProcessingTag(Result<?> message);
}
