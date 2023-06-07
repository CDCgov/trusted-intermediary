package gov.hhs.cdc.trustedintermediary.auth;

/**
 * The POJO that is parsed out of an auth login request from a client.
 *
 * @param scope The scope field that holds the organizations name.
 * @param jwt The JWT that comes out of the client_assertion field.
 */
public record AuthRequest(String scope, String jwt) {}
