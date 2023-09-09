package gov.hhs.cdc.trustedintermediary.etor.orders;

public interface TaskNotifier {
    void sendTaskId(Task<?> task);
}
