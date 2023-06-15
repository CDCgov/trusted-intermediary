package gov.hhs.cdc.trustedintermediary;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombinerException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Directs the construction of a full YAML OpenAPI specification */
public class OpenApi {

    private static final OpenApi INSTANCE = new OpenApi();

    private OpenApi() {}

    public static OpenApi getInstance() {
        return INSTANCE;
    }

    public String generateApiDocumentation(final Set<String> openApiSpecifications)
            throws UnableToReadOpenApiSpecificationException {
        openApiSpecifications.add(getBaselineDocumentation());

        try {
            // not using @Inject in a field of this class because we are still bootstrapping the
            // application context
            // also not using a static field because we need to register different YamlCombiners in
            // the unit tests
            return ApplicationContext.getImplementation(YamlCombiner.class)
                    .combineYaml(openApiSpecifications);
        } catch (YamlCombinerException e) {
            throw new UnableToReadOpenApiSpecificationException(
                    "Failed to combine YAML to generate API documentation", e);
        }
    }

    String getBaselineDocumentation() throws UnableToReadOpenApiSpecificationException {
        String fileName = "openapi_base.yaml";
        try (InputStream openApiStream =
                getClass().getClassLoader().getResourceAsStream(fileName)) {
            return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UnableToReadOpenApiSpecificationException(
                    "Failed to open baseline documentation for " + fileName, e);
        }
    }
}
