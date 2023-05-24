package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamLabOrderSender
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrganizationsSettingsTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrganizationsSettings, OrganizationsSettings.getInstance())
    }

    def "Load organizations from file"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        String yamlContent = """- name: test-name
  description: Test Description
"""

        Path tempFile = Files.createTempFile("organizations", ".yaml")
        Files.writeString(tempFile, yamlContent)
        String yamlFilePath = tempFile.toString()
        def settings = OrganizationsSettings.getInstance()

        when:
        settings.loadOrganizations(yamlFilePath)
        def organizations = settings.getOrganizations()

        then:
        organizations.size() == 1
        Organization organization = organizations[0]
        organization.getName() == "test-name"
        organization.getDescription() == "Test Description"

        cleanup:
        Files.deleteIfExists(tempFile)
    }
}
