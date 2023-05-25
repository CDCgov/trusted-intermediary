package gov.hhs.cdc.trustedintermediary.organizations;

import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Responsible for loading a list or organizations from a settings file, mapping them to a list of
 * {@link Organization} instances, and providing a way to verify an organization exists in the list.
 */
public class OrganizationsSettings {

    private static final OrganizationsSettings INSTANCE = new OrganizationsSettings();

    private List<Organization> organizations;

    @Inject private Formatter formatter;

    public static OrganizationsSettings getInstance() {
        return INSTANCE;
    }

    private OrganizationsSettings() {}

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void loadOrganizations(Path filePath) throws IOException, FormatterProcessingException {
        String organizationsFileString = Files.readString(filePath);
        this.organizations =
                formatter.convertYamlToObject(
                        organizationsFileString, new TypeReference<List<Organization>>() {});
    }

    public Optional<Organization> findOrganization(String name) {
        for (Organization organization : organizations) {
            if (organization.getName().equals(name)) {
                return Optional.of(organization);
            }
        }
        return Optional.empty();
    }
}
