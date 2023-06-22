package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
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

    public static final String AUTH_ENDPOINT = "/v1/auth";

    @Inject AuthController authController;
    @Inject RequestSessionTokenUsecase requestSessionTokenUsecase;
    @Inject Logger logger;

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(new HttpEndpoint("POST", AUTH_ENDPOINT), this::handleAuth);

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
            return authController.constructResponse(400);
        }

        var token = "";
        var payload = "";

        try {
            token = requestSessionTokenUsecase.getToken(authRequest);
        } catch (InvalidTokenException | UnknownOrganizationException e) {
            logger.logError("Authentication failed", e);
            return authController.constructResponse(401);
        } catch (Exception e) {
            logger.logFatal("Bad authentication service configuration: Authentication failed", e);
            return authController.constructResponse(500);
        }

        try {
            payload = authController.constructPayload(authRequest, token);
        } catch (Exception e) {
            logger.logFatal("Internal formatter exception occurred", e);
            return authController.constructResponse(500);
        }

        return authController.constructResponse(200, payload);
    }
}
