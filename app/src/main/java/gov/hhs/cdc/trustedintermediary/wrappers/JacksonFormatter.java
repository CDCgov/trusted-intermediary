package gov.hhs.cdc.trustedintermediary.wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonFormatter implements Formatter {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T convertToObject(String input, Class<T> clazz) throws FormatterProcessingException {
        try {
            return objectMapper.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            throw new FormatterProcessingException(
                    "Jackson's objectMapper failed to convert JSON to object");
        }
    }

    @Override
    public String convertToString(Object obj) throws FormatterProcessingException {

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new FormatterProcessingException(
                    "Jackson's objectMapper failed to convert object to JSON");
        }
    }
}
