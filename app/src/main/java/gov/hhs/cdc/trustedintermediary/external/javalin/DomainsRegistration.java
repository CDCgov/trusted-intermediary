package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.OpenApi;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registers the available domains to the application context and their specific handlers for the
 * endpoints.
 */
public class DomainsRegistration {

    // not using @Inject because we are still bootstrapping the application context
    private static final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);
    private static final OpenApi OPEN_API = ApplicationContext.getImplementation(OpenApi.class);

    public static void registerDomains(
            Javalin app, Set<Class<? extends DomainConnector>> domainConnectors) {

        LOGGER.logInfo("Info");
        LOGGER.logWarning("Warning");
        LOGGER.logDebug("Debug");
        LOGGER.logError("Error");

        var instantiatedDomains =
                domainConnectors.stream()
                        .map(DomainsRegistration::constructNewDomainConnector)
                        .collect(Collectors.toSet());

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
                                            app.addHandler(
                                                    HandlerType.valueOf(endpoint.verb()),
                                                    endpoint.path(),
                                                    createHandler(handler));
                                            LOGGER.logInfo(
                                                    "verb: "
                                                            + endpoint.verb()
                                                            + ", endpoint: "
                                                            + endpoint.path());
                                        }));
    }

    static void registerOpenApi(Javalin app, Set<DomainConnector> domains) {
        Set<String> openApiSpecifications =
                domains.stream()
                        .map(DomainConnector::openApiSpecification)
                        .collect(Collectors.toSet());
        String fullOpenApiSpecification = OPEN_API.generateApiDocumentation(openApiSpecifications);
        app.get(
                "/openapi",
                ctx -> {
                    ctx.header("Content-Type", "application/yaml");
                    ctx.result(fullOpenApiSpecification);
                });
    }

    static DomainConnector constructNewDomainConnector(Class<? extends DomainConnector> clazz) {
        try {
            Constructor<? extends DomainConnector> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    static Handler createHandler(Function<DomainRequest, DomainResponse> handler) {
        return (Context ctx) -> {
            LOGGER.logInfo(ctx.method().name() + " " + ctx.url());

            var request = javalinContextToDomainRequest(ctx);

            var response = handler.apply(request);

            domainResponseFillsInJavalinContext(response, ctx);

            LOGGER.logInfo("Handler complete");
        };
    }

    static DomainRequest javalinContextToDomainRequest(Context ctx) {
        var request = new DomainRequest();

        request.setBody(ctx.body());
        request.setUrl(ctx.url());
        request.setHeaders(ctx.headerMap());

        return request;
    }

    static void domainResponseFillsInJavalinContext(DomainResponse response, Context ctx) {
        ctx.status(response.getStatusCode());
        response.getHeaders().forEach(ctx::header);
        ctx.result(response.getBody());
    }
}
