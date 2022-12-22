package gov.hhs.cdc.trustedintermediary.wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonFormatter implements Formatter {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T convertToObject(String input, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(input, clazz);
    }

    @Override
    public String convertToString(Object obj) throws JsonProcessingException {

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }
}
