package gov.hhs.cdc.trustedintermediary.organizations;

import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public void loadOrganizations(Path filePath) throws OrganizationConfigException {
        try {
            String organizationsFileString = Files.readString(filePath);
            this.organizations =
                    formatter.convertYamlToObject(
                            organizationsFileString, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new OrganizationConfigException(
                    "Unable to read the configuration file " + filePath, exception);
        }
    }

    public void loadOrganizations() throws OrganizationConfigException {
        try (InputStream organizationStream =
                getClass().getClassLoader().getResourceAsStream("organizations.yaml")) {
            String rawOrganizationYamlString =
                    new String(organizationStream.readAllBytes(), StandardCharsets.UTF_8);
            this.organizations =
                    formatter.convertYamlToObject(
                            rawOrganizationYamlString, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new OrganizationConfigException(
                    "Unable to read the default configuration file", exception);
        }
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
