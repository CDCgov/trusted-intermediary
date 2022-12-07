package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.Slf4jLogger;
import io.javalin.Javalin;
import java.util.Set;

/** Creates the starting point of our API. Handles the registration of the domains. */
public class App {

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        app.get("/", ctx -> ctx.result("Welcome to the CDC TI service."));
        app.get("/health", ctx -> ctx.result("Operational"));

        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger());

        Set<Class<? extends DomainConnector>> domainConnectors =
                ApplicationContext.getImplementors(DomainConnector.class);

        DomainsRegistration.registerDomains(app, domainConnectors);
    }
}
