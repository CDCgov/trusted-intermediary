package gov.hhs.cdc.trustedintermediary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OpenApi {

    public String generateApiDocumentation(List<String> urls) {
        String openApiDocumentation = getBaselineDocumentation();

        openApiDocumentation += generatePerEndpointDocumentation(urls);

        return openApiDocumentation;
    }

    private String getBaselineDocumentation() {
        String baselineDocumentation;

        try {
            baselineDocumentation =
                    Files.readString(
                            Paths.get(
                                    getClass()
                                            .getClassLoader()
                                            .getResource("openapi_light.yaml")
                                            .toURI()),
                            StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return baselineDocumentation;
    }

    private String generatePerEndpointDocumentation(final List<String> urls) {
        return "";
    }
}
