package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.Reference
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ValidationRuleEngineIntegrationTest extends Specification {
    def testExampleFilesPath = "../examples/Test"
    def fhir = HapiFhirImplementation.getInstance()
    def engine = ValidationRuleEngine.getInstance("validation_definitions.json")
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.register(ValidationRuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "validation logs a warning when at least one validation fails"() {
        given:
        def bundle = new Bundle()

        when:
        engine.runRules(new HapiFhirResource(bundle))

        then:
        (1.._) * mockLogger.logWarning(_ as String)
    }

    def "validation doesn't break for any of the sample test messages"() {
        given:
        def exampleFhirResources = getExampleFhirResources("Orders")

        when:
        exampleFhirResources.each { resource ->
            engine.runRules(resource)
        }

        then:
        noExceptionThrown()
    }

    def "validation rule with resolve() works as expected"() {
        given:
        def fhirResource = getExampleFhirResource("e2e/orders/001_OML_O21_short.fhir")
        def validation = "Bundle.entry.resource.ofType(MessageHeader).focus.resolve().category.exists()"
        Rule rule = createValidationRule([], [validation])

        when:
        rule.runRule(fhirResource)

        then:
        0 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)
    }

    def "validation rules pass for test files"() {
        given:
        def fhirResource = getExampleFhirResource(testFile)
        def rule = createValidationRule([], [validation])
        0 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        expect:
        rule.runRule(fhirResource)

        where:
        testFile | validation
        "e2e/orders/001_OML_O21_short.fhir"                                       | "Bundle.entry.resource.ofType(MessageHeader).focus.resolve().category.exists()"
        "Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir" | "Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(system = 'urn:ietf:rfc:3986').value.exists()"
        // Once we fix the mapping for ORM from story #900 and update the FHIR files in /examples/Test/Orders, we can uncomment the below line
        // "Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir" | "Bundle.entry.resource.ofType(Observation).where(code.coding.code = '57723-9').value.coding.code.exists()"
    }

    def "validation rules fail for test files"() {
        given:
        def fhirResource = getExampleFhirResource(testFile)
        def rule = createValidationRule([], [validation])
        1 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        expect:
        rule.runRule(fhirResource)

        where:
        testFile | validation
        "e2e/orders/001_OML_O21_short.fhir" | "Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(system = 'urn:ietf:rfc:3986').value.exists()"
        "e2e/orders/001_OML_O21_short.fhir" | "Bundle.entry.resource.ofType(Observation).where(code.coding.code = '57723-9').value.coding.code.exists()"
    }

    def "validation passes: Message has required receiver id"() {
        given:
        def fhirPathValidation = "Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(system = 'urn:ietf:rfc:3986').value.exists()"
        def rule = createValidationRule([], [fhirPathValidation])

        when:
        Organization receiverOrganization = new Organization()
        receiverOrganization.setId(UUID.randomUUID().toString())
        receiverOrganization
                .addIdentifier()
                .setSystem("urn:ietf:rfc:3986")
                .setValue("simulated-hospital-id")
        def bundle = createMessageBundle(receiverOrganization: receiverOrganization)
        // for some reason, we need to encode and decode the bundle for resolve() to work
        def fhirResource = new HapiFhirResource(fhir.parseResource(fhir.encodeResourceToJson(bundle), Bundle))
        rule.runRule(fhirResource)

        then:
        0 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        when:
        receiverOrganization = new Organization()
        receiverOrganization.setId(UUID.randomUUID().toString())
        receiverOrganization
                .addIdentifier()
                .setSystem("another-system")
                .setValue("simulated-hospital-id")
        bundle = createMessageBundle(receiverOrganization: receiverOrganization)
        fhirResource = new HapiFhirResource(fhir.parseResource(fhir.encodeResourceToJson(bundle), Bundle))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)

        when:
        receiverOrganization = new Organization()
        receiverOrganization.setId(UUID.randomUUID().toString())
        receiverOrganization
                .addIdentifier()
                .setValue("simulated-hospital-id")
        bundle = createMessageBundle(receiverOrganization: receiverOrganization)
        fhirResource = new HapiFhirResource(fhir.parseResource(fhir.encodeResourceToJson(bundle), Bundle))
        rule.runRule(fhirResource)

        then:
        1 * mockLogger.logWarning(_ as String)
        0 * mockLogger.logError(_ as String, _ as Exception)
    }

    Bundle createMessageBundle(Map params) {
        String messageTypeCode = params.messageTypeCode as String ?: "ORM_O01"
        Organization receiverOrganization = params.receiverOrganization as Organization ?: new Organization()
        MessageHeader messageHeader = params.messageType as MessageHeader ?: new MessageHeader()

        MessageHeader.MessageDestinationComponent destination = messageHeader.addDestination()
        String receiverOrganizationFullUrl = "Organization/" + receiverOrganization.getId()
        destination.setReceiver(new Reference(receiverOrganizationFullUrl))

        Coding eventCoding = new Coding()
        eventCoding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0003")
        String[] parts = messageTypeCode.split("_")
        eventCoding.setCode(parts[1])
        eventCoding.setDisplay(String.format("%s^%s^%s", parts[0], parts[1], messageTypeCode))
        messageHeader.setEvent(eventCoding)

        Bundle bundle = new Bundle()
        bundle.setType(Bundle.BundleType.MESSAGE)
        bundle.addEntry().setResource(messageHeader)
        bundle.addEntry().setFullUrl(receiverOrganizationFullUrl).setResource(receiverOrganization)
        return bundle
    }

    Rule createValidationRule(List<String> ruleConditions, List<String> ruleValidations) {
        return new ValidationRule(
                name: "Rule name",
                description: "Rule description",
                message: "Rule warning message",
                conditions: ruleConditions,
                rules: ruleValidations,
                )
    }

    List<HapiFhirResource> getExampleFhirResources(String messageType = "") {
        return Files.walk(Path.of(testExampleFilesPath, messageType))
                .filter { it.toString().endsWith(".fhir") }
                .map { new HapiFhirResource(fhir.parseResource(Files.readString(it), Bundle))  }
                .collect()
    }

    HapiFhirResource getExampleFhirResource(String relativeFilePath) {
        return new HapiFhirResource(fhir.parseResource(Files.readString(Path.of(testExampleFilesPath, relativeFilePath)), Bundle))
    }
}
