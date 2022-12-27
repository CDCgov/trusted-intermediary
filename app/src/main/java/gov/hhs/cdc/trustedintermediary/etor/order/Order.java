package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    private String id;
    private String destination;
    private String createAt;
    private String client;
    private String content;
    private final DateTimeFormatter dateTimeFormat =
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

    public Order() {}

    // Ideal for testing
    public Order(String id, String destination, String createdAt, String client, String content) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
        setClient(client);
        setContent(content);
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public String getCreatedAt() {
        return createAt;
    }

    public void setId(String id) {
        this.id = (id == null) ? "" : id;
    }

    public void setDestination(String destination) {
        this.destination = (destination == null) ? "" : destination;
    }

    public void setCreatedAt(String createdAt) {
        LocalDateTime timestamp = null;
        try {
            timestamp = LocalDateTime.parse(createdAt);
        } catch (DateTimeException e) {
            LOGGER.logWarning("Improper format of createAt");
            throw new DateTimeException("improper format of datatime", e);
        }

        this.createAt = (createdAt == null) ? "" : timestamp.format(this.dateTimeFormat);
    }

    public void setCreateAt(String createdAt, DateTimeFormatter format) {
        LocalDateTime timestamp = LocalDateTime.parse(createdAt);
        this.createAt = timestamp.format(format);
    }
}
