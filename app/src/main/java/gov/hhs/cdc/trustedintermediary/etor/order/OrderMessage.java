package gov.hhs.cdc.trustedintermediary.etor.order;

public class OrderMessage {
    private String id;
    private String destination;
    private String createdAt;
    private String client;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public OrderMessage() {}

    public OrderMessage(String id, String destination, String createdAt, String client) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
        setClient(client);
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
