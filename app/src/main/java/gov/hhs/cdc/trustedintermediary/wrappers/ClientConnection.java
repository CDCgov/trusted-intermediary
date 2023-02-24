package gov.hhs.cdc.trustedintermediary.wrappers;

public interface ClientConnection {

    void sendRequestBody(String json);

    void requestToken();
}
