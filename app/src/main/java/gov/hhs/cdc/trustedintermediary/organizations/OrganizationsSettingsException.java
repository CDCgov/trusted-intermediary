package gov.hhs.cdc.trustedintermediary.organizations;

/** Exception thrown when the organizations settings configuration cannot be loaded. */
public class OrganizationsSettingsException extends Exception {
    public OrganizationsSettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
