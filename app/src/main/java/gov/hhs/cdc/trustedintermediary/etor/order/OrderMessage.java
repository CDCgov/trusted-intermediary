package gov.hhs.cdc.trustedintermediary.etor.order;

import javax.annotation.Nonnull;

/**
 * Contains similar information for the order that is sent back to the client as confirmation that
 * an order was ingested.
 */
public class OrderMessage {

    private String id;
    private String destination;
    private String createdAt;
    private String client;
    private String content;

    public OrderMessage(
            String id, String destination, String createdAt, String client, String content) {
        setId(id);
        setDestination(destination);
        setCreatedAt(createdAt);
        setClient(client);
        setContent(content);
    }

    public OrderMessage(@Nonnull Order order) {
        setId(order.getId());
        setDestination(order.getDestination());
        setCreatedAt(order.getCreatedAt());
        setClient(order.getClient());
        setContent(order.getContent());
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
}
