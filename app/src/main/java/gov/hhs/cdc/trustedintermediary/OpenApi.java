package gov.hhs.cdc.trustedintermediary;

import java.util.List;

public class OpenApi {

    public String generateApiDocumentation(List<String> urls) {
        String openAPI = "";
        openAPI =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("openapi_light.yaml");
    }
}
