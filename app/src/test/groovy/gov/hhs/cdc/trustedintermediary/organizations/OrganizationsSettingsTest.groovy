package gov.hhs.cdc.trustedintermediary.organizations

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrganizationsSettingsTest extends Specification {

    Path tempFile
    OrganizationsSettings settings

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Organization, Organization)
        TestApplicationContext.register(OrganizationsSettings, OrganizationsSettings.getInstance())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        String yamlContent = "- name: test-name\n  description: Test Description"
        tempFile = Files.createTempFile("organizations", ".yaml")
        Files.writeString(tempFile, yamlContent)
        settings = OrganizationsSettings.getInstance()
    }

    def cleanup() {
        Files.deleteIfExists(tempFile)
    }

    def "Load organizations from yaml file works"() {
        given:
        def expectedOrganizationsSize = 1

        when:
        settings.loadOrganizations(tempFile)
        def organizations = settings.getOrganizations()
        def actualOrganizationsSize = organizations.size()

        then:
        actualOrganizationsSize == expectedOrganizationsSize
    }

    def "Properties for loaded organizations match expected values"() {
        given:
        settings.loadOrganizations(tempFile)
        def expectedOrganizationName = "test-name"
        def expectedOrganizationDescription = "Test Description"

        when:
        def organizations = settings.getOrganizations()
        Organization organization = organizations[0]
        def actualOrganizationName = organization.getName()
        def actualOrganizationDescription = organization.getDescription()

        then:
        actualOrganizationName == expectedOrganizationName
        actualOrganizationDescription == expectedOrganizationDescription
    }

    def "findOrganization returns correct organization by name when exists"() {
        given:
        settings.loadOrganizations(tempFile)
        def expectedOrganizationName = "test-name"

        when:
        def organization = settings.findOrganization(expectedOrganizationName)
        def actualOrganizationName = organization.get().getName()

        then:
        organization.isPresent()
        actualOrganizationName == expectedOrganizationName
    }

    def "findOrganization returns empty optional when organization does not exist"() {
        given:
        settings.loadOrganizations(tempFile)
        def expectedOrganizationName = "non-existent-name"

        when:
        def organization = settings.findOrganization(expectedOrganizationName)

        then:
        !organization.isPresent()
    }
}
