package gov.hhs.cdc.trustedintermediary;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
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

    public String generateApiDocumentation(final Set<String> openApiSpecifications) {
        openApiSpecifications.add(getBaselineDocumentation());
        openApiSpecifications.add(getEtorDocumentation());

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
        try (InputStream openApiStream =
                getClass().getClassLoader().getResourceAsStream("openapi_base.yaml")) {
            return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String getEtorDocumentation() {
        try (InputStream openApiStream =
                getClass().getClassLoader().getResourceAsStream("openapi_etor.yaml")) {
            return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
