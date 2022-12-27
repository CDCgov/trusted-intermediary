package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.awt.*;

public class OrderMessage {
    private String id;
    private String destination;
    private String createdAt;
    private String client;
    private String content;
    private final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);
    private Formatter formatter = ApplicationContext.getImplementation(Formatter.class);

    public OrderMessage() {}

    public OrderMessage(
            String id, String destination, String createdAt, String client, String content) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
        setClient(client);
        setContent(content);
    }

    public OrderMessage(Order order) {
        if (order != null) {
            setId(order.getId());
            setDestination(order.getDestination());
            setCreatedAt(order.getCreatedAt());
            setClient(order.getClient());
            setContent(order.getContent());
        }
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String generateMessage() {
        OrderMessage tempOrderMessage = new OrderMessage();
        checkAndLogMissingFields();
        tempOrderMessage.setDestination(this.destination);
        tempOrderMessage.setId(this.id);
        tempOrderMessage.setCreatedAt(this.createdAt);
        tempOrderMessage.setClient(this.client);
        tempOrderMessage.setContent(this.content);
        LOGGER.logInfo("Generating order message...");

        try {
            return formatter.convertToString(this);
        } catch (FormatterProcessingException e) {
            LOGGER.logWarning("Formatter was unable to convert orderMessage object to string");

            throw new RuntimeException(e);
        }
    }

    private void checkAndLogMissingFields() {
        this.id = (this.id != null && !this.id.isEmpty()) ? this.id : "missing id";
        if (this.id.equals("missing id")) {
            LOGGER.logWarning("Missing order id");
        }
        this.destination =
                (this.destination != null && !this.destination.isEmpty())
                        ? this.destination
                        : "missing destination";
        if (this.destination.equals("missing destination")) {
            LOGGER.logWarning("Missing order destination");
        }

        this.createdAt =
                (this.createdAt != null && !this.createdAt.isEmpty())
                        ? this.createdAt
                        : "missing timestamp";
        if (this.createdAt.equals("missing timestamp")) {
            LOGGER.logWarning("Missing order timestamp");
        }

        this.client =
                (this.client != null && !this.client.isEmpty()) ? this.client : "missing client";
        if (this.client.equals("missing client")) {
            LOGGER.logWarning("Missing order client");
        }

        this.content =
                (this.content != null && !this.content.isEmpty())
                        ? this.content
                        : "missing content";
        if (this.content.equals("missing content")) {
            LOGGER.logWarning("Missing order content");
        }
    }
}
