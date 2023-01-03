package gov.hhs.cdc.trustedintermediary.etor.order;

/** Represents an ETOR order. */
public class Order {
    private String id;
    private String destination;
    private String createdAt;

    private String client;
    private String content;

    // for serialization
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
}
