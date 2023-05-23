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

public class OrganizationsLoader {

    private static final OrganizationsLoader INSTANCE = new OrganizationsLoader();

    private List<Organization> organizations;

    @Inject private Formatter formatter;

    public static OrganizationsLoader getInstance() {
        return INSTANCE;
    }

    private OrganizationsLoader() {}

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void loadOrganizations(String filePath)
            throws IOException, FormatterProcessingException {
        String organizationsFileString = Files.readString(Paths.get(filePath));
        this.organizations =
                formatter.convertToObject(
                        organizationsFileString, new TypeReference<List<Organization>>() {});
    }

    public Optional<Sender> findSender(String name) {
        for (Organization organization : organizations) {
            for (Sender sender : organization.getSenders()) {
                if (sender.getName().equals(name)) {
                    return Optional.of(sender);
                }
            }
        }
        return Optional.empty();
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
