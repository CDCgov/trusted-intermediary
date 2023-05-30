package gov.hhs.cdc.trustedintermediary.organizations;

/** Exception thrown when the organizations settings configuration cannot be loaded. */
public class OrganizationConfigException extends Exception {
    public OrganizationConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
