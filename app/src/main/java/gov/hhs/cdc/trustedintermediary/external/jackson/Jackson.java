package gov.hhs.cdc.trustedintermediary.external.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner;
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombinerException;
import java.util.HashMap;
import java.util.Set;
import javax.inject.Inject;

/** A wrapper around the Jackson library that implements some helpers. */
public class Jackson implements Formatter, YamlCombiner {

    private static final Jackson INSTANCE = new Jackson();

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_OBJECT_MAPPER =
            new ObjectMapper(
                    new YAMLFactory()
                            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                            .disable(YAMLGenerator.Feature.SPLIT_LINES)
                            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));

    @Inject Logger logger;

    private Jackson() {}

    public static Jackson getInstance() {
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
