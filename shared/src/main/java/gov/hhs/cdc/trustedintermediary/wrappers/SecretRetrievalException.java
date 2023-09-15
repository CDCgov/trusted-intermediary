package gov.hhs.cdc.trustedintermediary.wrappers;

/** Occurs when a secret can't be retrieved. */
public class SecretRetrievalException extends Exception {

    public SecretRetrievalException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
