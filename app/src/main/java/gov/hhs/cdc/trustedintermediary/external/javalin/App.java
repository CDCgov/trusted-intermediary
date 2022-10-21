package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.Function;

/** Creates the starting point of our API. Handles the registration of the domains. */
public class App {

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        app.get("/health", ctx -> ctx.result("Operational"));

        Set<Class<? extends DomainConnector>> domainConnectors =
                ApplicationContext.getImplementors(DomainConnector.class);

        domainConnectors.stream()
                .map(App::constructNewDomainConnector)
                .map(DomainConnector::domainRegistration)
                .forEach(
                        registrationMap ->
                                registrationMap.forEach(
                                        (verbPath, handler) ->
                                                app.addHandler(
                                                        HandlerType.valueOf(verbPath.getVerb()),
                                                        verbPath.getPath(),
                                                        createHandler(handler))));
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

    private static Handler createHandler(Function<DomainRequest, DomainResponse> handler) {
        return (Context ctx) -> {
            var request = javalinContextToDomainRequest(ctx);

            var response = handler.apply(request);

            domainResponseFillsInJavalinContext(response, ctx);
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
