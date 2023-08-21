package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SentPayloadReader {

    private static final Path SENT_PAYLOAD_PATH = Path.of("..", "app", "localfileorder.json");

    public static String read() throws IOException {
        return Files.readString(SENT_PAYLOAD_PATH);
    }

    public static void delete() throws IOException {
        Files.deleteIfExists(SENT_PAYLOAD_PATH);
    }
}
