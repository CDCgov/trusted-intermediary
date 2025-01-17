package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.OpenApi;
import gov.hhs.cdc.trustedintermediary.auth.AuthRequestValidator;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnectorConstructionException;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper;
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.router.Endpoint;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registers the available domains to the application context and their specific handlers for the
 * endpoints.
 *
 * <p>Also registers the OpenAPI specification(s) for the given domain
 */
public class DomainsRegistration {

    // not using @Inject because we are still bootstrapping the application context
    private static final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

    private DomainsRegistration() {}

    public static void registerDomains(
            Javalin app, Set<Class<? extends DomainConnector>> domainConnectors)
            throws UnableToReadOpenApiSpecificationException, DomainConnectorConstructionException {

        var instantiatedDomains = new HashSet<DomainConnector>();
        for (Class<? extends DomainConnector> domainConnector : domainConnectors) {
            DomainConnector connector = constructNewDomainConnector(domainConnector);
            instantiatedDomains.add(connector);
        }

        registerDomainsWithApplicationContext(instantiatedDomains);

        registerDomainsHandlers(app, instantiatedDomains);

        registerOpenApi(app, instantiatedDomains);
    }

    static void registerDomainsWithApplicationContext(Set<DomainConnector> domains) {
        domains.forEach(domain -> ApplicationContext.register(domain.getClass(), domain));
    }

    static void registerDomainsHandlers(Javalin app, Set<DomainConnector> domains) {
        LOGGER.logInfo("Registering: ");
        domains.stream()
                .map(DomainConnector::domainRegistration)
                .forEach(
                        registrationMap ->
                                registrationMap.forEach(
                                        (endpoint, handler) -> {
                                            app.addEndpoint(
                                                    new Endpoint(
                                                            HandlerType.valueOf(endpoint.verb()),
                                                            endpoint.path(),
                                                            createHandler(
                                                                    handler,
                                                                    endpoint.isProtected())));
                                            LOGGER.logInfo(
                                                    "verb: "
                                                            + endpoint.verb()
                                                            + ", endpoint: "
                                                            + endpoint.path());
                                        }));
    }

    static void registerOpenApi(Javalin app, Set<DomainConnector> domains)
            throws UnableToReadOpenApiSpecificationException {
        Set<String> openApiSpecifications = new HashSet<>();
        for (DomainConnector domain : domains) {
            openApiSpecifications.add(domain.openApiSpecification());
        }

        // not using @Inject in a field of this class because we are still bootstrapping the
        // application context
        // also not using a static field because we need to register different YamlCombiners in the
        // unit tests
        String fullOpenApiSpecification =
                ApplicationContext.getImplementation(OpenApi.class)
                        .generateApiDocumentation(openApiSpecifications);

        app.get(
                "/openapi",
                ctx -> {
                    LOGGER.logInfo(ctx.method().name() + " " + ctx.url());
                    ctx.header("Content-Type", "application/x-yaml");
                    ctx.result(fullOpenApiSpecification);
                });
    }

    static DomainConnector constructNewDomainConnector(Class<? extends DomainConnector> clazz)
            throws DomainConnectorConstructionException {
        try {
            Constructor<? extends DomainConnector> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException exception) {
            throw new DomainConnectorConstructionException(
                    "Failed to construct new domain connector " + clazz, exception);
        }
    }

    static Handler createHandler(
            Function<DomainRequest, DomainResponse> handler, boolean isProtected) {
        return (Context ctx) -> {
            ApplicationContext
                    .clearThreadRegistrations(); // clear this thread's specific registrations from
            // its previous use

            LOGGER.logInfo(ctx.method().name() + " " + ctx.url());

            var request = javalinContextToDomainRequest(ctx);
            DomainResponse response = processRequest(request, handler, isProtected);
            domainResponseFillsInJavalinContext(response, ctx);

            LOGGER.logInfo("Handler complete");
        };
    }

    protected static DomainResponse processRequest(
            DomainRequest request,
            Function<DomainRequest, DomainResponse> handler,
            boolean isProtected) {
        if (isProtected) {
            DomainResponse authResponse = authenticateRequest(request);
            // if authResponse is not null, it means authentication was not successful
            // and we need to return the DomainResponse
            if (authResponse != null) {
                return authResponse;
            }
        }

        return handler.apply(request);
    }

    protected static DomainResponse authenticateRequest(DomainRequest request) {
        AuthRequestValidator authValidator =
                ApplicationContext.getImplementation(AuthRequestValidator.class);
        DomainResponseHelper domainResponseHelper =
                ApplicationContext.getImplementation(DomainResponseHelper.class);
        LOGGER.logDebug("Authenticating request...");
        try {
            if (!authValidator.isValidAuthenticatedRequest(request)) {
                var errorMessage = "The request failed the authentication check";
                LOGGER.logError(errorMessage);
                return domainResponseHelper.constructErrorResponse(401, errorMessage);
            }
        } catch (SecretRetrievalException | IllegalArgumentException e) {
            LOGGER.logFatal("Unable to validate whether the request is authenticated", e);
            return domainResponseHelper.constructErrorResponse(500, e);
        }

        LOGGER.logInfo("Request successfully validated");
        return null;
    }

    static DomainRequest javalinContextToDomainRequest(Context ctx) {
        var request = new DomainRequest();
        var caseInsensitiveHeaderMap =
                ctx.headerMap().entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        entry -> entry.getKey().toLowerCase(),
                                        Map.Entry::getValue));

        request.setBody(ctx.body());
        request.setUrl(ctx.url());
        request.setHeaders(caseInsensitiveHeaderMap);
        request.setPathParams(ctx.pathParamMap());

        return request;
    }

    static void domainResponseFillsInJavalinContext(DomainResponse response, Context ctx) {
        ctx.status(response.getStatusCode());
        response.getHeaders().forEach(ctx::header);
        ctx.result(response.getBody());
    }
}
