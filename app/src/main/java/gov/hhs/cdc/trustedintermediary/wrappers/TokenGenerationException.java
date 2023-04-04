package gov.hhs.cdc.trustedintermediary.wrappers;

public class TokenGenerationException extends Exception {
    public TokenGenerationException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
