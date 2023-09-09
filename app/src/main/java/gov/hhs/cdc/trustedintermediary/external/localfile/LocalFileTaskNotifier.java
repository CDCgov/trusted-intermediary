package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.tasks.Task;
import gov.hhs.cdc.trustedintermediary.etor.tasks.TaskNotifier;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.inject.Inject;

public class LocalFileTaskNotifier implements TaskNotifier {

    static final String LOCAL_FILE_NAME = "localfiletask.json";
    private static final LocalFileTaskNotifier INSTANCE = new LocalFileTaskNotifier();

    @Inject Formatter formatter;
    @Inject Logger logger;

    public static LocalFileTaskNotifier getInstance() {
        return INSTANCE;
    }

    private LocalFileTaskNotifier() {}

    @Override
    public void sendTaskId(final Task<?> task) {
        var fileLocation = Paths.get(LOCAL_FILE_NAME);
        logger.logInfo("Sending the task to the hard drive at {}", fileLocation.toAbsolutePath());

        try {
            String serialized = formatter.convertToJsonString(Map.of("taskId", task.getTaskId()));
            Files.writeString(fileLocation, serialized, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error writing the task", e);
        }
    }
}
