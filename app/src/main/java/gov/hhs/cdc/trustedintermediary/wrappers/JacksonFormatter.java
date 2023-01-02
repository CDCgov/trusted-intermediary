package gov.hhs.cdc.trustedintermediary.wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;

public class JacksonFormatter implements Formatter {

    private static final JacksonFormatter INSTANCE = new JacksonFormatter();

    private static final ObjectMapper objectMapper =
            new ObjectMapper(); // Look into objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true)
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
            logger.logError("Jackson's objectMapper failed to convert JSON to object", e);
            throw new FormatterProcessingException(
                    "Jackson's objectMapper failed to convert JSON to object", e);
        }
    }

    @Override
    public String convertToString(Object obj) throws FormatterProcessingException {

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.logError("Jackson's objectMapper failed to convert object to JSON", e);
            throw new FormatterProcessingException(
                    "Jackson's objectMapper failed to convert object to JSON", e);
        }
    }
}
