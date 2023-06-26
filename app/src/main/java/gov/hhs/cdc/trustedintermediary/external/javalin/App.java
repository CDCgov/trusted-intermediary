package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.OpenApi;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnectorConstructionException;
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import gov.hhs.cdc.trustedintermediary.external.apache.ApacheClient;
import gov.hhs.cdc.trustedintermediary.external.azure.AzureSecrets;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation;
import gov.hhs.cdc.trustedintermediary.external.inmemory.KeyCache;
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson;
import gov.hhs.cdc.trustedintermediary.external.jjwt.JjwtEngine;
import gov.hhs.cdc.trustedintermediary.external.localfile.LocalSecrets;
import gov.hhs.cdc.trustedintermediary.external.slf4j.Slf4jLogger;
import gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import io.javalin.Javalin;
import java.util.Set;

/** Creates the starting point of our API. Handles the registration of the domains. */
public class App {

    static final String HEALTH_API_ENDPOINT = "/health";

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        // apply this security header to all responses, but allow it to be overwritten by a specific
        // endpoint by using `before` if needed
        app.before(ctx -> ctx.header("X-Content-Type-Options", "nosniff"));

        try {
            app.get(HEALTH_API_ENDPOINT, ctx -> ctx.result("Operational"));

            registerClasses();
            registerDomains(app);
            ApplicationContext.injectRegisteredImplementations();
            OrganizationsSettings.getInstance().loadOrganizations();
        } catch (Exception exception) {
            // Not using the logger because boostrapping has failed.
            System.out.println(
                    "Exception occurred during bootstrap of Trusted Intermediary!  Exiting!");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void registerDomains(Javalin app)
            throws DomainConnectorConstructionException, UnableToReadOpenApiSpecificationException {
        Set<Class<? extends DomainConnector>> domainConnectors =
                ApplicationContext.getImplementors(DomainConnector.class);

        DomainsRegistration.registerDomains(app, domainConnectors);
    }

    private static void registerClasses() {
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger());
        ApplicationContext.register(Formatter.class, Jackson.getInstance());
        ApplicationContext.register(HapiFhir.class, HapiFhirImplementation.getInstance());
        ApplicationContext.register(YamlCombiner.class, Jackson.getInstance());
        ApplicationContext.register(OpenApi.class, OpenApi.getInstance());
        ApplicationContext.register(HttpClient.class, ApacheClient.getInstance());
        ApplicationContext.register(AuthEngine.class, JjwtEngine.getInstance());
        ApplicationContext.register(Cache.class, KeyCache.getInstance());
        ApplicationContext.register(
                Secrets.class,
                ApplicationContext.getEnvironment().equalsIgnoreCase("local")
                        ? LocalSecrets.getInstance()
                        : AzureSecrets.getInstance());
        ApplicationContext.register(
                OrganizationsSettings.class, OrganizationsSettings.getInstance());
    }
}
