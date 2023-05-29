package gov.hhs.cdc.trustedintermediary.auth;

public record AuthRequest(String scope, String jwt) {}
