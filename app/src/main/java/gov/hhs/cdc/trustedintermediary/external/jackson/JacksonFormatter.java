package gov.hhs.cdc.trustedintermediary.external.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import gov.hhs.cdc.trustedintermediary.wrappers.*;
import java.util.HashMap;
import java.util.Set;
import javax.inject.Inject;

/** A {@link Formatter} that converts to and from JSON using the Jackson library. */
public class JacksonFormatter implements Formatter, YamlCombiner {

    private static final JacksonFormatter INSTANCE = new JacksonFormatter();

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper(); // for JSON
    private static final ObjectMapper YAML_OBJECT_MAPPER =
            new ObjectMapper(
                    new YAMLFactory()
                            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                            .disable(YAMLGenerator.Feature.SPLIT_LINES)
                            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));

    @Inject Logger logger;

    private JacksonFormatter() {}

    public static JacksonFormatter getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> T convertToObject(String input, Class<T> clazz) throws FormatterProcessingException {
        try {
            return JSON_OBJECT_MAPPER.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            String errorMessage = "Jackson's objectMapper failed to convert JSON to object";
            logger.logError(errorMessage, e);
            throw new FormatterProcessingException(errorMessage, e);
        }
    }

    @Override
    public String convertToString(Object obj) throws FormatterProcessingException {

        try {
            return JSON_OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            String errorMessage = "Jackson's objectMapper failed to convert object to JSON";
            logger.logError(errorMessage, e);
            throw new FormatterProcessingException(errorMessage, e);
        }
    }

    @Override
    public String combineYaml(final Set<String> yamlStrings) throws YamlCombinerException {
        var mapOfMaps = new HashMap<>();
        var yamlObjectUpdater = YAML_OBJECT_MAPPER.readerForUpdating(mapOfMaps);

        for (String yaml : yamlStrings) {
            try {
                yamlObjectUpdater.readValue(yaml);
            } catch (JsonProcessingException e) {
                throw new YamlCombinerException("Unable to parse the YAML passed in", e);
            }
        }

        try {
            return YAML_OBJECT_MAPPER.writeValueAsString(mapOfMaps);
        } catch (JsonProcessingException e) {
            throw new YamlCombinerException("Unable to serialize combined YAML", e);
        }
    }
}
