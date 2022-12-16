package gov.hhs.cdc.trustedintermediary.etor.order;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    private String id;
    private String destination;
    private String createAt;
    private OrderMessage orderMessage = ApplicationContext.getImplementation(OrderMessage.class);
    private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

    public Order() {}

    // Ideal for testing
    public Order(String id, String destination, LocalDateTime createdAt) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
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
        this.id = id;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createAt = createdAt.format(this.dateTimeFormat);
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
        // if the variables are null, add ""
        if (this.id == null) {
            this.id = "missing id";
        }
        if (this.destination == null) {
            this.destination = "missing destination";
        }
        if (this.createAt == null) {
            this.createAt = "missing timestamp";
        }
        this.orderMessage.setDestination(this.destination);
        this.orderMessage.setId(this.id);
        this.orderMessage.setCreatedAt(this.createAt);
        return this;
    }
}
