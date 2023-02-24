package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SentPayloadReader {
    /*
    1. Enhance findFilePayload to find the file if it is somewhere else.
    2. Improving our understanding of JSON data so we can make specific assertions against sub-fields in a JSON blob.
     */

    public static String read() throws IOException {
        var payloadFile = findFilePayload();

        return Files.readString(payloadFile);
    }

    public static Path findFilePayload() {
        return Path.of("..", "app", "localfilelaborder.json");
    }
}
