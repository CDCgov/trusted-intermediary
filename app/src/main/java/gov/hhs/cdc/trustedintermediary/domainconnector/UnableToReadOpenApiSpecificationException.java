package gov.hhs.cdc.trustedintermediary.domainconnector;

public class UnableToReadOpenApiSpecificationException extends Exception {
    public UnableToReadOpenApiSpecificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
