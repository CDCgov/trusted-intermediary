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

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        app.get("/health", ctx -> ctx.result("Operational"));

        // TODO: this should be dynamic to get the different registrations
        var etorRegistration = new DomainRegistration();

        var registrationDetails = etorRegistration.domainRegistration();

        registrationDetails.forEach(
                (verbPath, handler) ->
                        app.addHandler(
                                HandlerType.valueOf(verbPath.getVerb()),
                                verbPath.getPath(),
                                createHandler(handler)));
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
