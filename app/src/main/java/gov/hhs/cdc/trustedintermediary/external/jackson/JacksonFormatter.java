package gov.hhs.cdc.trustedintermediary.external.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner;
import java.util.HashMap;
import java.util.Set;
import javax.inject.Inject;

/** A {@link Formatter} that converts to and from JSON using the Jackson library. */
public class JacksonFormatter implements Formatter, YamlCombiner {

    private static final JacksonFormatter INSTANCE = new JacksonFormatter();

    private static final ObjectMapper objectMapper = new ObjectMapper(); // for JSON
    private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    @Inject Logger logger;

    private JacksonFormatter() {}

    public static JacksonFormatter getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> T convertToObject(String input, Class<T> clazz) throws FormatterProcessingException {
        try {
            return objectMapper.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            String errorMessage = "Jackson's objectMapper failed to convert JSON to object";
            logger.logError(errorMessage, e);
            throw new FormatterProcessingException(errorMessage, e);
        }
    }

    @Override
    public String convertToString(Object obj) throws FormatterProcessingException {

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            String errorMessage = "Jackson's objectMapper failed to convert object to JSON";
            logger.logError(errorMessage, e);
            throw new FormatterProcessingException(errorMessage, e);
        }
    }

    @Override
    public String combineYaml(final Set<String> yamlStrings) {
        var mapOfMaps = new HashMap<>();
        var yamlObjectUpdater = YAML_OBJECT_MAPPER.readerForUpdating(mapOfMaps);

        yamlStrings.forEach(
                yaml -> {
                    try {
                        yamlObjectUpdater.readValue(yaml);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        try {
            return YAML_OBJECT_MAPPER.writeValueAsString(mapOfMaps);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
