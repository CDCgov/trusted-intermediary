package gov.hhs.cdc.trustedintermediary;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombinerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class OpenApi {

    private static final OpenApi INSTANCE = new OpenApi();

    // not using @Inject because we are still bootstrapping the application context
    private static final YamlCombiner YAML_COMBINER =
            ApplicationContext.getImplementation(YamlCombiner.class);

    private OpenApi() {}

    public static OpenApi getInstance() {
        return INSTANCE;
    }

    public String generateApiDocumentation(Set<String> openApiSpecifications) {
        openApiSpecifications.add(getBaselineDocumentation());

        try {
            return YAML_COMBINER.combineYaml(openApiSpecifications);
        } catch (YamlCombinerException e) {
            assert false;
        }
        assert false;
        return "";
    }

    private String getBaselineDocumentation() {
        String baselineDocumentation;

        try {
            baselineDocumentation =
                    Files.readString(
                            Paths.get(
                                    getClass()
                                            .getClassLoader()
                                            .getResource("openapi_base.yaml")
                                            .toURI()),
                            StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return baselineDocumentation;
    }
}
