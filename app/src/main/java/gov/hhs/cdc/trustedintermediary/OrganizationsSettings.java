package gov.hhs.cdc.trustedintermediary;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

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

    public void loadOrganizations(String filePath)
            throws IOException, FormatterProcessingException {
        String organizationsFileString = Files.readString(Paths.get(filePath));
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
