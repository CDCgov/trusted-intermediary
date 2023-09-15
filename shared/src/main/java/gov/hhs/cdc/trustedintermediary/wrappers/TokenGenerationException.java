package gov.hhs.cdc.trustedintermediary.wrappers;

/** Thrown when the {@link AuthEngine} cannot create a token. */
public class TokenGenerationException extends Exception {
    public TokenGenerationException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
