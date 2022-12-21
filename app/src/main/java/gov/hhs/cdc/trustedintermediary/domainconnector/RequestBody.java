package gov.hhs.cdc.trustedintermediary.domainconnector;

public class RequestBody {
    private String content;

    public RequestBody() {}

    public RequestBody(String content) {
        setContent(content);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
