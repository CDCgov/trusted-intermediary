package gov.hhs.cdc.trustedintermediary.etor.order;

public class OrderMessage {
    private String id;
    private String destination;
    private String createdAt;

    public OrderMessage() {}

    public OrderMessage(String id, String destination, String createdAt) {
        this.id = id;
        this.destination = destination;
        this.createdAt = createdAt;
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
}
