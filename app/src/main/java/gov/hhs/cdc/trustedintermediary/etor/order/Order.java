package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    private String id;
    private String destination;
    private String createAt;
    private String client;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    private OrderMessage orderMessage = ApplicationContext.getImplementation(OrderMessage.class);
    private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Order() {}

    // Ideal for testing
    public Order(String id, String destination, LocalDateTime createdAt, String client) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
        setClient(client);
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
        // TODO Logger
        if (this.id == null | this.id == "") {
            this.id = "missing id";
        }
        if (this.destination == null | this.destination == "") {
            this.destination = "missing destination";
        }
        if (this.createAt == null | this.createAt == "") {
            this.createAt = "missing timestamp";
        }
        if (this.client == null | this.client == "") {
            this.client = "missing client";
        }
        this.orderMessage.setDestination(this.destination);
        this.orderMessage.setId(this.id);
        this.orderMessage.setCreatedAt(this.createAt);
        this.orderMessage.setClient(this.client);
        return this;
    }
}
