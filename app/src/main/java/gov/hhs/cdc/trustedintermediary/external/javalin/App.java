package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.OpenApi;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson;
import gov.hhs.cdc.trustedintermediary.external.slf4j.Slf4jLogger;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner;
import io.javalin.Javalin;
import java.util.Set;

/** Creates the starting point of our API. Handles the registration of the domains. */
public class App {

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        try {
            app.get("/health", ctx -> ctx.result("Operational"));

            registerClasses();
            registerDomains(app);
            ApplicationContext.injectRegisteredImplementations();
        } catch (Exception exception) {
            // Not using the logger because boostrapping has failed.
            System.err.println(
                    "Exception occurred during bootstrap of Trusted Intermediary!  Exiting!");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void registerDomains(Javalin app) {
        Set<Class<? extends DomainConnector>> domainConnectors =
                ApplicationContext.getImplementors(DomainConnector.class);

        DomainsRegistration.registerDomains(app, domainConnectors);
    }

    private static void registerClasses() {
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger());
        ApplicationContext.register(Formatter.class, Jackson.getInstance());
        ApplicationContext.register(YamlCombiner.class, Jackson.getInstance());
        ApplicationContext.register(OpenApi.class, OpenApi.getInstance());
    }
}
