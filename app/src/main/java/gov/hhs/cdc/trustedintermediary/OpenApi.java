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

    private OpenApi() {}

    public static OpenApi getInstance() {
        return INSTANCE;
    }

    public String generateApiDocumentation(final Set<String> openApiSpecifications) {
        openApiSpecifications.add(getBaselineDocumentation());

        try {
            // not using @Inject in a field of this class because we are still bootstrapping the
            // application context
            // also not using a static field because we need to register different YamlCombiners in
            // the unit tests
            return ApplicationContext.getImplementation(YamlCombiner.class)
                    .combineYaml(openApiSpecifications);
        } catch (YamlCombinerException e) {
            throw new RuntimeException(e);
        }
    }

    String getBaselineDocumentation() {
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
