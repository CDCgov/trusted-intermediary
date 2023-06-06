package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
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
    @Inject Formatter formatter;

    private AuthController() {}

    public static AuthController getInstance() {
        return AUTH_CONTROLLER;
    }

    public AuthRequest parseAuthRequest(DomainRequest request) throws IllegalArgumentException {
        logger.logInfo("Parsing login request via JWT");

        Map<String, Optional<String>> authFields = extractFormUrlEncode(request.getBody());

        return new AuthRequest(
                authFields.getOrDefault("scope", Optional.empty()).orElse(null),
                authFields.getOrDefault("client_assertion", Optional.empty()).orElse(null));
    }

    public DomainResponse constructResponse(int httpStatus) {
        return constructResponse(httpStatus, null);
    }

    public DomainResponse constructResponse(int httpStatus, String payload) {

        DomainResponse response = new DomainResponse(httpStatus);

        if (payload != null) {
            response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JWT_LITERAL));
            response.setBody(payload);
        }

        return response;
    }

    protected Map<String, Optional<String>> extractFormUrlEncode(String body)
            throws IllegalArgumentException {
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

    protected String constructPayload(AuthRequest authRequest, String token)
            throws FormatterProcessingException {

        String payloadJson;
        String bearerToken = "";

        if (token != null && !token.isBlank()) {
            bearerToken = token;
        }

        String scope = authRequest.scope();
        String tokenType = "bearer";
        Map<String, String> payload = new HashMap<>();

        payload.put("token_type", token_type);
        payload.put("access_token", bearerToken);
        payload.put("scope", scope);

        payloadJson = formatter.convertToJsonString(payload);

        return payloadJson;
    }
}
