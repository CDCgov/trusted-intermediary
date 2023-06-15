package gov.hhs.cdc.trustedintermediary.domainconnector;

/** Thrown when an OpenAPI specification cannot be read. */
public class UnableToReadOpenApiSpecificationException extends Exception {
    public UnableToReadOpenApiSpecificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
