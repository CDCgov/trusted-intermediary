package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException;
import javax.inject.Inject;

/** TODO */
public class RequestSessionTokenUsecase {

    @Inject private AuthEngine auth;

    private static final RequestSessionTokenUsecase INSTANCE = new RequestSessionTokenUsecase();

    public static RequestSessionTokenUsecase getInstance() {
        return INSTANCE;
    }

    private RequestSessionTokenUsecase() {}

    public String getToken(AuthRequest request) {

        // Validate the JWT is signed by a trusted entity
        // TODO get key from Azure and/or cache
        var rsPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlDsjJmbSl1R/9F8HeyJgT5tMVJp7Svk6N80R+LitxwgNqd9SUSaLjTG662MssViR1nPsy2j/ieLVvKPCj51DRW5h5kVcaumEQxacm6MjOUGPYQ0Y1j8dWxWlkNqH1iRowXZH6ABHcwcecWWyf/lCRt12b0I+n5TJ/F8VVzJ7jRAjHkaOLHmM5tUI1dTJZAReh/qlXQmgjl9u2Pn4YazK8zYYnvplTvif+HuoIeR+Cll7w63Ue6/2OJVTOvblYpx7TG9ZHVEZDnoIks/cvRDnZKShLPql9RHDt5JhsVrFCdOdWa4IOw/IdSXWT/+VzmBiJQw9hhV53IPSUVyp/YaN0QIDAQAB"; // pragma: allowlist secret
        try {
            auth.validateToken(request.jwt(), rsPublicKey);
        } catch (InvalidTokenException e) {

        } catch (IllegalArgumentException e) {

        }

        // TODO Provide a short-lived access token for subsequent calls to the TI service
        String token = "";
        try {
            // token = auth.generateToken(key, scope);

        } catch (Exception e) {
            // TODO
        }
        return token;
    }
}
