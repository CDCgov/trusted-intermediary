package gov.hhs.cdc.trustedintermediary.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;

public class JsonParser {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    public static Map<String, ?> parse(String json) throws JsonProcessingException {
        return JSON_OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
    }

    public static Map<String, ?> parse(InputStream stream) throws IOException {
        return JSON_OBJECT_MAPPER.readValue(stream, new TypeReference<>() {});
    }

    public static Map<String, ?> parseContent(ClassicHttpResponse response) throws IOException {
        return parse(response.getEntity().getContent());
    }
}
