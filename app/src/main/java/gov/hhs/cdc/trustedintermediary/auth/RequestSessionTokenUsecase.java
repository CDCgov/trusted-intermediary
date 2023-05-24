package gov.hhs.cdc.trustedintermediary.auth;

/** TODO */
public class RequestSessionTokenUsecase {

    private static final RequestSessionTokenUsecase INSTANCE = new RequestSessionTokenUsecase();

    public static RequestSessionTokenUsecase getInstance() {
        return INSTANCE;
    }

    private RequestSessionTokenUsecase() {}

    public String getToken(AuthRequest request) {

        // Validate the JWT is signed by a trusted entity

        // Provide a short-lived access token for subsequent calls to the TI service

        return "{session='token'}";
    }
}
