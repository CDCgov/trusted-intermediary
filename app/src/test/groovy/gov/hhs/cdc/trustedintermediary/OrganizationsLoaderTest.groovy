package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class OrganizationsLoaderTest extends Specification {
    def "Load organizations from file"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        String yamlContent = """- name: test-name
  description: Test Description
  senders:
    - name: test-sender-name
      topic: test-sender-topic
      customerStatus: test-sender-customer-status
      format: test-sender-format
  receivers:
"""

        Path tempFile = Files.createTempFile("organizations", ".yaml")
        Files.writeString(tempFile, yamlContent)
        String yamlFilePath = tempFile.toString()
        def loader = OrganizationsLoader.getInstance()

        when:
        loader.loadOrganizations(yamlFilePath)
        def organizations = loader.getOrganizations()

        then:
        organizations.size() == 1
        Organization organization = organizations[0]
        organization.getName() == "test-name"
        organization.getDescription() == "Test Description"
        organization.getSenders().size() == 1
        Sender sender = organization.getSenders()[0]
        sender.getName() == "test-sender-name"
        sender.getTopic() == Sender.Topic.TEST
        sender.getCustomerStatus() == Sender.CustomerStatus.TEST
        sender.getFormat() == Sender.Format.TEST
        organization.getReceivers().size() == 0

        cleanup:
        Files.deleteIfExists(tempFile)
    }
}
