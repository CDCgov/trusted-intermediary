package gov.hhs.cdc.trustedintermediary.wrappers;

/** Occurs when an invalid JWT is parsed */
public class InvalidTokenException extends Exception {

    public InvalidTokenException(Throwable e) {
        super(e);
    }
}
