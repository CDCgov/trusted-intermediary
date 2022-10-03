package gov.hhs.cdc.trustedintermediary;

import io.javalin.Javalin;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        var app = Javalin.create().start(8080);

        app.get("/", ctx -> ctx.result(new App().getGreeting()));
    }
}
