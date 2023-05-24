package gov.hhs.cdc.trustedintermediary.organizations

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrganizationsSettingsTest extends Specification {

    Path tempFile

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
    }

    def cleanup() {
        Files.deleteIfExists(tempFile)
    }

    def "Load organizations from yaml file works"() {
        given:
        def settings = OrganizationsSettings.getInstance()
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
        def settings = OrganizationsSettings.getInstance()
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

    def "findOrganization works"() {
        given:
        def settings = OrganizationsSettings.getInstance()
        settings.loadOrganizations(tempFile)
        def expectedOrganizationName = "test-name"

        when:
        def organization = settings.findOrganization(expectedOrganizationName)
        def actualOrganizationName = organization.get().getName()

        then:
        organization.isPresent()
        actualOrganizationName == expectedOrganizationName
    }
}
