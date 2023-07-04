package gov.hhs.cdc.trustedintermediary.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.hc.core5.http.ClassicHttpResponse;

public class JsonParsing {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    public static Map parse(String json) throws JsonProcessingException {
        return JSON_OBJECT_MAPPER.readValue(json, Map.class);
    }

    public static Map parse(InputStream stream) throws IOException {
        return JSON_OBJECT_MAPPER.readValue(stream, Map.class);
    }

    public static Map parseContent(ClassicHttpResponse response) throws IOException {
        return parse(response.getEntity().getContent());
    }
}
