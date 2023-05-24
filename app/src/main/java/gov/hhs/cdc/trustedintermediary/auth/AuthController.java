package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Map;
import javax.inject.Inject;

/** TODO */
public class AuthController {

    private static final AuthController AUTH_CONTROLLER = new AuthController();
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JWT_LITERAL = "application/jwt";

    @Inject private AuthEngine jwt;
    @Inject Logger logger;

    private AuthController() {}

    public static AuthController getInstance() {
        return AUTH_CONTROLLER;
    }

    public AuthRequest parseAuthRequest(DomainRequest request) {
        logger.logInfo("Parsing login request via JWT");

        // Map<String> jwt = Jwts.parserBuilder().build().parsePlaintextJws(request.getBody());

        return new AuthRequest("ReportStream", "{jwt='here'}"); // TODO
    }

    public DomainResponse constructResponse(String accessToken) {

        if (accessToken == null) {
            return new DomainResponse(401);
        } else {
            DomainResponse response = new DomainResponse(200);
            response.setBody(accessToken);
            response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JWT_LITERAL));
            return response;
        }
    }
}
