package gov.hhs.cdc.trustedintermediary;

import java.util.List;

public class Organization {
    private String name;
    private String description;
    private List<Sender> senders;
    private List<Receiver> receivers;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Sender> getSenders() {
        return senders;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }
}

class Sender {
    private String name;
    private Topic topic;
    private CustomerStatus customerStatus;
    private Format format;

    enum Format {
        CSV,
        HL7,
        FHIR,
        HL7_BATCH,
        TEST;
    }

    enum CustomerStatus {
        ACTIVE,
        INACTIVE,
        TEST;
    }

    enum Topic {
        ETOR,
        TEST;
    }

    public String getName() {
        return name;
    }

    public Topic getTopic() {
        return topic;
    }

    public CustomerStatus getCustomerStatus() {
        return customerStatus;
    }

    public Format getFormat() {
        return format;
    }
}

class Receiver {
    // TODO: Fields will depend on the structure of the receiver YAML
}
