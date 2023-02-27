package gov.hhs.cdc.trustedintermediary.e2e;

import java.io.IOException;
import java.nio.file.Files;

public class SentPayloadReader {
    /*
    1. Enhance findFilePayload to find the file if it is somewhere else.
    2. Add tests for the sent payload
     */

    public static String read() throws IOException {
        var payloadFile = findFilePayload();

        return Files.readString(payloadFile);
    }

    //    public static Path findFilePayload() {
    //        Path.of("..", "localfilelaborder.json").
    //                //ways to check if a file exists, and return that path
    //                //throw an exception if not found (done by line 16)
    //        return Path.of("..", "app", "localfilelaborder.json");
    //    }
}
