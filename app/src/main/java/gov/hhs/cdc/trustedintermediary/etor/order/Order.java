package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    private String id;
    private String destination;
    private String createAt;
    private String client;
    private String body;
    private OrderMessage orderMessage = new OrderMessage();
    private final DateTimeFormatter dateTimeFormat =
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private final Logger LOGGER = ApplicationContext.getImplementation(Logger.class);

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Order() {}

    // Ideal for testing
    public Order(
            String id, String destination, LocalDateTime createdAt, String client, String body) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
        setClient(client);
        setBody(body);
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createAt = (createdAt == null) ? "" : createdAt.format(this.dateTimeFormat);
    }

    public void setCreateAt(LocalDateTime createdAt, DateTimeFormatter format) {
        this.createAt = createdAt.format(format);
    }

    public OrderMessage getOrderMessage() {
        return orderMessage;
    }

    public void setOrderMessage(OrderMessage orderMessage) {
        this.orderMessage = orderMessage;
    }

    public Order generateMessage() {
        OrderMessage tempOrderMessage = new OrderMessage();
        if (this.id == null | this.id == "") {
            LOGGER.logWarning("Order missing id.");
            this.id = "missing id";
        }
        if (this.destination == null | this.destination == "") {
            LOGGER.logWarning("Order missing destination.");
            this.destination = "missing destination";
        }
        if (this.createAt == null | this.createAt == "") {
            LOGGER.logWarning("Order missing timestamp.");
            this.createAt = "missing timestamp";
        }
        if (this.client == null | this.client == "") {
            LOGGER.logWarning("Order missing client.");
            this.client = "missing client";
        }
        if (this.body == null | this.body == "") {
            LOGGER.logWarning("Order missing body.");
            this.body = "missing body";
        }
        tempOrderMessage.setDestination(this.destination);
        tempOrderMessage.setId(this.id);
        tempOrderMessage.setCreatedAt(this.createAt);
        tempOrderMessage.setClient(this.client);
        tempOrderMessage.setBody(this.body);
        setOrderMessage(tempOrderMessage);
        return this;
    }
}
