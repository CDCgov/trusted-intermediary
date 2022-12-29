package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Order {
    private String id;
    private String destination;
    private String createdAt;

    private String client;
    private String content;
    private final DateTimeFormatter dateTimeFormat =
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

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
        return createdAt;
    }

    public void setId(String id) {
        this.id = (id == null) ? "" : id;
    }

    public void setDestination(String destination) {
        this.destination = (destination == null) ? "" : destination;
    }

    public void setCreatedAt(String createdAt) {

        this.createdAt = createdAt;
    }

    //    public void setCreateAt(LocalDateTime createdAt, DateTimeFormatter format) {
    //        LocalDateTime timestamp = LocalDateTime.parse(createdAt);
    //        this.createAt = timestamp.format(format);
    //    }

    private boolean isValidDateTime(LocalDateTime createdAt) {
        String dateTimeFormatRegex =
                "\\b(0[1-9]|1[0-2])(\\s|-){1}([1-9]|[12][0-9]|3[01])(\\s|-){1}(19[0-9][0-9]|20[0-2][0-3])(\\s)([1-9]|1[0-9]|2[01234])(:)(0[0-9]|[1-5][0-9])\\b";
        String ldtRegex =
                "\\b(19[0-9][0-9]|20[0-2][0-3])(-)"
                        + "(0[1-9]|[1-9]|1[0-2])(-)"
                        + "(0[1-9]|[1-9]|[12][0-9]|3[01])(T)"
                        + "(0[1-9]|[1-5][0-9])(:)"
                        + "(0[1-9]|[1-5][0-9])(:)"
                        + "(0[1-9]|[1-5][0-9])([\\.]\\d+)?\\b";
        Pattern pattern = Pattern.compile(ldtRegex);
        return pattern.matcher(createdAt.toString()).matches();
    }
}
