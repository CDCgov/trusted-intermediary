package gov.hhs.cdc.trustedintermediary.etor.tasks;

public interface TaskNotifier {
    void sendTaskId(Task<?> task);
}
