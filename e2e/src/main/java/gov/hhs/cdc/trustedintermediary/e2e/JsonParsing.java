package gov.hhs.cdc.trustedintermediary.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParsing {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    public static <T> T parse(String json, Class<T> clazz) throws JsonProcessingException {
        return JSON_OBJECT_MAPPER.readValue(json, clazz);
    }
}
