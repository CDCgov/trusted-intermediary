package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SentPayloadReader {
    /*
    1. Enhance findFilePayload to find the file if it is somewhere else.
    2. Add tests for the sent payload
     */

    public static String read() throws IOException {
        Path payloadFile = null;
        try {
            payloadFile = findFilePayload();
        } catch (Exception e) {
            throw new FileNotFoundException("File not found");
        }

        return Files.readString(payloadFile);
    }

    public static Path findFilePayload() {
        // TODO
        // ways to check if a file exists, and return that path
        // throw an exception if not found (done by line 16)
        return Path.of("..", "app", "localfilelaborder.json");
    }
}
