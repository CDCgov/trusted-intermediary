package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.*;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

/**
 * The domain connector for the Auth domain. It connects it with the larger trusted intermediary.
 */
public class AuthDomainRegistration implements DomainConnector {

    static final String AUTH_API_ENDPOINT = "/v1/auth/token";

    @Inject AuthController authController;
    @Inject DomainResponseHelper domainResponseHelper;
    @Inject RequestSessionTokenUsecase requestSessionTokenUsecase;
    @Inject Logger logger;

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(new HttpEndpoint("POST", AUTH_API_ENDPOINT), this::handleAuth);

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(AuthController.class, AuthController.getInstance());
        ApplicationContext.register(
                RequestSessionTokenUsecase.class, RequestSessionTokenUsecase.getInstance());
        ApplicationContext.register(AuthRequestValidator.class, AuthRequestValidator.getInstance());
        return endpoints;
    }

    @Override
    public String openApiSpecification() throws UnableToReadOpenApiSpecificationException {
        String fileName = "openapi_auth.yaml";
        try (InputStream openApiStream =
                getClass().getClassLoader().getResourceAsStream(fileName)) {
            return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UnableToReadOpenApiSpecificationException(
                    "Failed to open OpenAPI specification for " + fileName, e);
        }
    }

    DomainResponse handleAuth(DomainRequest request) {
        AuthRequest authRequest;
        try {
            authRequest = authController.parseAuthRequest(request);
        } catch (Exception e) {
            logger.logError("Failed to parse the request", e);
            return domainResponseHelper.constructErrorResponse(400, e);
        }

        var token = "";

        try {
            token = requestSessionTokenUsecase.getToken(authRequest);
        } catch (InvalidTokenException | UnknownOrganizationException e) {
            logger.logError("Authentication failed", e);
            return domainResponseHelper.constructErrorResponse(401, e);
        } catch (Exception e) {
            var message = "Bad authentication service configuration";
            logger.logFatal(message, e);
            return domainResponseHelper.constructErrorResponse(500, message);
        }

        return authController.constructAuthenticatedResponse(token, authRequest.scope());
    }
}
