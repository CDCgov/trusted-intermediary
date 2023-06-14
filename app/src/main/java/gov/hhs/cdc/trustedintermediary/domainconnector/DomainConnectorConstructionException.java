package gov.hhs.cdc.trustedintermediary.domainconnector;

/** Thrown when a domain connector fails to be constructed. */
public class DomainConnectorConstructionException extends Exception {
    public DomainConnectorConstructionException(String message, Throwable cause) {
        super(message, cause);
    }
}
