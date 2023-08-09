package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SentPayloadReader {

    public static String read() throws IOException {
        Path payloadFile = findFilePayload();

        return Files.readString(payloadFile);
    }

    private static Path findFilePayload() {

        Path expectedFilePath = Path.of("..", "examples", "fhir/lab_order.json");

        return expectedFilePath;
    }
}
