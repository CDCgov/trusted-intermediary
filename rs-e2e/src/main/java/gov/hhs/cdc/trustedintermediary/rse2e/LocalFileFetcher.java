package gov.hhs.cdc.trustedintermediary.rse2e;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFileFetcher implements FileFetcher {

    private static final String FILES_PATH = "../examples/Test/Automated/";
    private static final String EXTENSION = "hl7";
    private static final FileFetcher INSTANCE = new LocalFileFetcher();

    private LocalFileFetcher() {}

    public static FileFetcher getInstance() {
        return INSTANCE;
    }

    @Override
    public List<HL7FileStream> fetchFiles() {
        try (Stream<Path> stream = Files.walk(Paths.get(FILES_PATH))) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(
                            p -> {
                                try {
                                    InputStream inputStream = new FileInputStream(p.toFile());
                                    return new HL7FileStream(
                                            p.getFileName().toString(), inputStream);
                                } catch (IOException e) {
                                    throw new RuntimeException("Error opening file: " + p, e);
                                }
                            })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
