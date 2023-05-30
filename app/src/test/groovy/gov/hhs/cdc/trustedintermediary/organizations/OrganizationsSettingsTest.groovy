package gov.hhs.cdc.trustedintermediary.organizations

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrganizationsSettingsTest extends Specification {

    Path tempFile

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrganizationsSettings, OrganizationsSettings.getInstance())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        String yamlContent = "- name: test-name\n  description: Test Description"
        tempFile = Files.createTempFile("organizations", ".yaml")
        Files.writeString(tempFile, yamlContent)
    }

    def cleanup() {
        Files.deleteIfExists(tempFile)
    }

    def "Load organizations from yaml file works"() {
        when:
        OrganizationsSettings.getInstance().loadOrganizations(tempFile)
        def organizations = OrganizationsSettings.getInstance().getOrganizations()
        def actualOrganizationsSize = organizations.size()

        then:
        actualOrganizationsSize == 1
    }

    def "Properties for loaded organizations match expected values"() {
        given:
        OrganizationsSettings.getInstance().loadOrganizations(tempFile)

        when:
        def organizations = OrganizationsSettings.getInstance().getOrganizations()
        Organization organization = organizations[0]
        def actualOrganizationName = organization.getName()
        def actualOrganizationDescription = organization.getDescription()

        then:
        actualOrganizationName == "test-name"
        actualOrganizationDescription == "Test Description"
    }

    def "findOrganization returns correct organization by name when exists"() {
        given:
        OrganizationsSettings.getInstance().loadOrganizations(tempFile)
        def expectedOrganizationName = "test-name"

        when:
        def organization = OrganizationsSettings.getInstance().findOrganization(expectedOrganizationName)
        def actualOrganizationName = organization.get().getName()

        then:
        organization.isPresent()
        actualOrganizationName == expectedOrganizationName
    }

    def "findOrganization returns empty optional when organization does not exist"() {
        given:
        OrganizationsSettings.getInstance().loadOrganizations(tempFile)

        when:
        def organization = OrganizationsSettings.getInstance().findOrganization("non-existent-name")

        then:
        !organization.isPresent()
    }
}
