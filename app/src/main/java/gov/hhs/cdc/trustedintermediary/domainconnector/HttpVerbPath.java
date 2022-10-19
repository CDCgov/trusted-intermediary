package gov.hhs.cdc.trustedintermediary.domainconnector;

public class HttpVerbPath {
    private String verb;
    private String path;

    public HttpVerbPath(String verb, String path) {
        this.verb = verb;
        this.path = path;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
