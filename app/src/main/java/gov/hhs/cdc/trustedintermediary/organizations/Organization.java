package gov.hhs.cdc.trustedintermediary.organizations;

/** Represents an organization in the organizations settings file. */
public class Organization {
    private String name;
    private String description;

    public Organization() {}
    ;

    public Organization(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
