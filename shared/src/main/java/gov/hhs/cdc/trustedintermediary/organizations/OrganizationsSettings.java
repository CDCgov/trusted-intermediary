package gov.hhs.cdc.trustedintermediary.organizations;

import gov.hhs.cdc.trustedintermediary.external.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.external.formatter.TypeReference;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Responsible for loading a list or organizations from a settings file, mapping them to a list of
 * {@link Organization} instances, and providing a way to verify an organization exists in the list.
 */
public class OrganizationsSettings {

    private static final OrganizationsSettings INSTANCE = new OrganizationsSettings();
    protected static String defaultOrganizationFile = "organizations.yaml";

    private Map<String, Organization> organizations = new HashMap<>();

    @Inject private Formatter formatter;

    public static OrganizationsSettings getInstance() {
        return INSTANCE;
    }

    private OrganizationsSettings() {}

    public Map<String, Organization> getOrganizations() {
        return organizations;
    }

    public void loadOrganizations(Path filePath) throws OrganizationsSettingsException {
        try {
            String organizationsFileString = Files.readString(filePath);
            var organizationList =
                    formatter.convertYamlToObject(
                            organizationsFileString, new TypeReference<List<Organization>>() {});

            organizations =
                    organizationList.stream()
                            .collect(Collectors.toMap(Organization::getName, Function.identity()));
        } catch (Exception exception) {
            throw new OrganizationsSettingsException(
                    "Unable to read the configuration file " + filePath, exception);
        }
    }

    public void loadOrganizations() throws OrganizationsSettingsException {
        try (InputStream organizationStream =
                getClass().getClassLoader().getResourceAsStream(defaultOrganizationFile)) {
            String rawOrganizationYamlString =
                    new String(organizationStream.readAllBytes(), StandardCharsets.UTF_8);
            var organizationList =
                    formatter.convertYamlToObject(
                            rawOrganizationYamlString, new TypeReference<List<Organization>>() {});

            organizations =
                    organizationList.stream()
                            .collect(Collectors.toMap(Organization::getName, Function.identity()));
        } catch (Exception exception) {
            throw new OrganizationsSettingsException(
                    "Unable to read the default configuration file", exception);
        }
    }

    public Optional<Organization> findOrganization(String name) {

        if (organizations.containsKey(name)) {
            Organization organization = organizations.get(name);
            return Optional.of(organization);
        }

        return Optional.empty();
    }
}
