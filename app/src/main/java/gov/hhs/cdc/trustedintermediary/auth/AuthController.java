package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import javax.inject.Inject;

/** TODO */
public class AuthController {

    private static final AuthController AUTH_CONTROLLER = new AuthController();
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JSON_LITERAL = "application/jwt";

    @Inject private AuthEngine jwt;

    private AuthController() {}

    public static AuthController getInstance() {
        return AUTH_CONTROLLER;
    }

    public String login(String jwt) {

        // TODO: Validate the JWT is signed by a trusted entity

        // TODO: Provide a short-lived access token for subsequent calls to the TI service

        return "ok!";
    }
}
