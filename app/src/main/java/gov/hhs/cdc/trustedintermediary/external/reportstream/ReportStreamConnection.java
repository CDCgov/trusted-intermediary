package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import java.io.IOException;
import javax.inject.Inject;

public class ReportStreamConnection implements ClientConnection {

    private String token = "";
    private final String URI = "http://reportstream.endpoint";
    @Inject private HttpClient client;

    private ReportStreamConnection() {}

    private static final ReportStreamConnection INSTANCE = new ReportStreamConnection();

    public static ReportStreamConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendRequestBody(String json) {
        String res;
        try {
            res = client.setToken(this.token).post(URI, json); // what to do with response?
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ReportStreamConnection setToken(String token) {
        this.token = token;
        return this;
    }

    public String requestToken() {
        // GET request
        // If successful, verify it's coming from RS
        return null;
    }

    //    public void foo() {
    //        byte[] key = requestToken().getBytes();
    //
    //        String jwt = Jwts.builder().setIssuer("RS")
    //                .setSubject("user/blahblah")
    //                .setExpiration(expirationDate)
    //                .put("scope", "self RS/waters")
    //                .SignWith(SignatureAlgorithm.HS256,key)
    //                .compact();
    //    }
    //
    //    public boolean boo(Jws jwt) {
    //
    //        Jws jwtclaims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
    //
    //        return false;
    //    }
}
