package gov.hhs.cdc.trustedintermediary.etor.order;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private String destination;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(int id, String destination, LocalDateTime createdAt) {
        this.id = id;
        this.destination = destination;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
