package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.etor.order.Order;
import gov.hhs.cdc.trustedintermediary.etor.order.OrderMessage;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.JacksonFormatter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.Slf4jLogger;
import io.javalin.Javalin;
import java.util.Set;

/** Creates the starting point of our API. Handles the registration of the domains. */
public class App {

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        app.get("/health", ctx -> ctx.result("Operational"));

        registerClasses();
        registerDomains(app);
        ApplicationContext.injectRegisteredImplementations();
    }

    private static void registerDomains(Javalin app) {
        Set<Class<? extends DomainConnector>> domainConnectors =
                ApplicationContext.getImplementors(DomainConnector.class);

        DomainsRegistration.registerDomains(app, domainConnectors);
    }

    private static void registerClasses() {
        ApplicationContext.register(Logger.class, Slf4jLogger.getLogger());
        ApplicationContext.register(Formatter.class, new JacksonFormatter());
        ApplicationContext.register(OrderMessage.class, new OrderMessage());
        ApplicationContext.register(Order.class, new Order());
    }
}
