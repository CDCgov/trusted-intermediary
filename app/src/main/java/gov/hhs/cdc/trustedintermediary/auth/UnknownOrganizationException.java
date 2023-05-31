package gov.hhs.cdc.trustedintermediary.auth;

/**
 * Thrown when an organization is not found in the {@link
 * gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings}.
 */
public class UnknownOrganizationException extends Exception {
    public UnknownOrganizationException(String message) {
        super(message);
    }
}
