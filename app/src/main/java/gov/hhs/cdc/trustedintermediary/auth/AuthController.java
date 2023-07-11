package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper;
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

    @Inject Logger logger;
    @Inject DomainResponseHelper domainResponseHelper;

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

    public DomainResponse constructAuthenticatedResponse(String token, String client) {
        var payload = Map.of("token_type", "bearer", "access_token", token, "scope", client);
        return domainResponseHelper.constructOkResponse(payload);
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
}
