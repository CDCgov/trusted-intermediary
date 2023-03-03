package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SentPayloadReader {

    public static String read() throws IOException {
        Path payloadFile = findFilePayload();

        return Files.readString(payloadFile);
    }

    public static Path findFilePayload() {

        Path expectedFilePath = Path.of("..", "app", "localfilelaborder.json");

        boolean doesFileExist = Files.exists(expectedFilePath);

        if (!doesFileExist) {
            expectedFilePath = Path.of("..", "localfilelaborder.json");
            doesFileExist = Files.exists(expectedFilePath);
        }

        return expectedFilePath;
    }
}
