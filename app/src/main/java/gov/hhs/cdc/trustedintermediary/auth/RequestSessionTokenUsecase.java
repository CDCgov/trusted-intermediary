package gov.hhs.cdc.trustedintermediary.auth;

import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** TODO */
public class RequestSessionTokenUsecase {

    private static final RequestSessionTokenUsecase INSTANCE = new RequestSessionTokenUsecase();

    public static RequestSessionTokenUsecase getInstance() {
        return INSTANCE;
    }

    private RequestSessionTokenUsecase() {}

    public String getToken(AuthRequest request) {

        // Validate the JWT is signed by a trusted entity
        var rsPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlDsjJmbSl1R/9F8HeyJgT5tMVJp7Svk6N80R+LitxwgNqd9SUSaLjTG662MssViR1nPsy2j/ieLVvKPCj51DRW5h5kVcaumEQxacm6MjOUGPYQ0Y1j8dWxWlkNqH1iRowXZH6ABHcwcecWWyf/lCRt12b0I+n5TJ/F8VVzJ7jRAjHkaOLHmM5tUI1dTJZAReh/qlXQmgjl9u2Pn4YazK8zYYnvplTvif+HuoIeR+Cll7w63Ue6/2OJVTOvblYpx7TG9ZHVEZDnoIks/cvRDnZKShLPql9RHDt5JhsVrFCdOdWa4IOw/IdSXWT/+VzmBiJQw9hhV53IPSUVyp/YaN0QIDAQAB"; // pragma: allowlist secret

        try {
            byte[] encode = Base64.getDecoder().decode(rsPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encode);
            var key = keyFactory.generatePublic(keySpec);
            var jwt = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(request.jwt());

            System.out.println(jwt);

            // TODO Catch invalid tokens, signed by incorrect alg, untrusted keys, and expired
            // tokens
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO Provide a short-lived access token for subsequent calls to the TI service

        return "{session='token'}";
    }
}
