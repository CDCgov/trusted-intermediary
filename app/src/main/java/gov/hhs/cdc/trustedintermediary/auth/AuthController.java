package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

/** Parses the request and generates the response that comes in for the auth endpoints. */
public class AuthController {

    private static final AuthController AUTH_CONTROLLER = new AuthController();
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JWT_LITERAL = "application/jwt";

    @Inject Logger logger;

    private AuthController() {}

    public static AuthController getInstance() {
        return AUTH_CONTROLLER;
    }

    public AuthRequest parseAuthRequest(DomainRequest request) {
        logger.logInfo("Parsing login request via JWT");

        Map<String, Optional<String>> authFields = extractFormUrlEncode(request.getBody());

        return new AuthRequest(
                authFields.get("scope").orElse(null),
                authFields.get("client_assertion").orElse(null));
    }

    public DomainResponse constructResponse(int httpStatus) {
        return constructResponse(httpStatus, null);
    }

    public DomainResponse constructResponse(int httpStatus, String accessToken) {

        DomainResponse response = new DomainResponse(httpStatus);

        if (accessToken != null) {
            response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JWT_LITERAL));
            response.setBody(accessToken);
        }

        return response;
    }

    protected Map<String, Optional<String>> extractFormUrlEncode(String body) {
        return Arrays.stream(body.split("&"))
                .map(entry -> entry.split("=", 2))
                .collect(
                        Collectors.toMap(
                                array -> URLDecoder.decode(array[0], StandardCharsets.UTF_8),
                                array -> {
                                    try {
                                        return Optional.of(
                                                URLDecoder.decode(
                                                        array[1], StandardCharsets.UTF_8));
                                    } catch (IndexOutOfBoundsException e) {
                                        return Optional.empty();
                                    }
                                }));
    }
}
