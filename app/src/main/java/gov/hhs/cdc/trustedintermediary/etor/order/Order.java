package gov.hhs.cdc.trustedintermediary.etor.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;

public class Order {
    private String id;
    private String destination;
    private String createAt;
    @Inject private OrderMessage orderMessage;
    private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");

    public Order() {}

    // Ideal for testing
    public Order(String id, String destination, LocalDateTime createdAt) {
        this.id = id;
        this.destination = destination;
        this.createAt = createdAt.format(dateTimeFormat);
        this.orderMessage.setDestination(this.destination);
        this.orderMessage.setId(this.id);
        this.orderMessage.setCreatedAt(this.createAt);
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
}
