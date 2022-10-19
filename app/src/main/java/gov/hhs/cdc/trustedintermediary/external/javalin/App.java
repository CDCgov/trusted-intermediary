package gov.hhs.cdc.trustedintermediary.external.javalin;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.etor.DomainRegistration;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import java.util.function.Function;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        // TODO: this should be dynamic to get the different registrations
        var etorRegistration = new DomainRegistration();

        var registrationDetails = etorRegistration.domainRegistration();

        registrationDetails.entrySet().stream()
                .forEach(
                        (var entry) -> {
                            var verbPath = entry.getKey();
                            var handler = entry.getValue();

                            app.addHandler(
                                    HandlerType.valueOf(verbPath.getVerb()),
                                    verbPath.getPath(),
                                    createHandler(handler));
                        });

        app.get("/", ctx -> ctx.result(new App().getGreeting()));
    }

    private static Handler createHandler(Function<DomainRequest, DomainResponse> handler) {
        return (Context ctx) -> {
            var request = javalinContextToDomainRequest(ctx);

            var response = handler.apply(request);

            domainResponseFillsInJavalinContext(response, ctx);
        };
    }

    private static DomainRequest javalinContextToDomainRequest(Context ctx) {
        var request = new DomainRequest();

        request.setBody(ctx.body());
        request.setUrl(ctx.url());
        request.setHeaders(ctx.headerMap());

        return request;
    }

    private static void domainResponseFillsInJavalinContext(DomainResponse response, Context ctx) {
        ctx.status(response.getStatusCode());
        response.getHeaders().entrySet().stream()
                .forEach(
                        entry -> {
                            ctx.header(entry.getKey(), entry.getValue());
                        });
        ctx.result(response.getBody());
    }
}
