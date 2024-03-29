package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleEngineIntegrationTest extends Specification {
    def testExampleFilesPath = "../examples/Test"
    def fhir = HapiFhirImplementation.getInstance()
    def engine = RuleEngine.getInstance()
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.register(RuleEngine, engine)
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "validation logs a warning when at least one validation fails"() {
        given:
        def bundle = new Bundle()

        when:
        engine.validate(new HapiFhirResource(bundle))

        then:
        (1.._) * mockLogger.logWarning(_ as String)
    }

    def "validation doesn't break for any of the sample test messages"() {
        given:
        def exampleFhirResources = getExampleFhirResources("Orders")

        when:
        exampleFhirResources.each { resource ->
            engine.validate(resource)
        }

        then:
        noExceptionThrown()
    }

    def "validation rule with resolve() works as expected"() {
        given:
        def fhirResource = getExampleFhirResource("Orders/001_OML_O21_short.fhir")
        def validation = "Bundle.entry.resource.ofType(MessageHeader).focus.resolve().category.exists()"
        def rule = createValidationRule([], [validation])

        when:
        def applies = rule.isValid(fhirResource)

        then:
        applies
    }

    def "validation rules pass for test files"() {
        given:
        def fhirResource = getExampleFhirResource(testFile)
        def rule = createValidationRule([], [validation])

        expect:
        rule.isValid(fhirResource)

        where:
        testFile | validation
        "Orders/001_OML_O21_short.fhir"                                    | "Bundle.entry.resource.ofType(MessageHeader).focus.resolve().category.exists()"
        "Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir" | "Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.value.exists()"
        // Once we fix the mapping for ORM from story #900 and update the FHIR files in /examples/Test/Orders, we can uncomment the below line
        // "Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir" | "Bundle.entry.resource.ofType(Observation).where(code.coding.code = '57723-9').value.coding.code.exists()"
    }

    def "validation rules fail for test files"() {
        given:
        def fhirResource = getExampleFhirResource(testFile)
        def rule = createValidationRule([], [validation])

        expect:
        !rule.isValid(fhirResource)

        where:
        testFile | validation
        "Orders/001_OML_O21_short.fhir" | "Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.value.exists()"
        "Orders/001_OML_O21_short.fhir" | "Bundle.entry.resource.ofType(Observation).where(code.coding.code = '57723-9').value.coding.code.exists()"
    }

    Rule createValidationRule(List<String> ruleConditions, List<String> ruleValidations) {
        return new ValidationRule(
                name: "Rule name",
                description: "Rule description",
                violationMessage: "Rule warning message",
                conditions: ruleConditions,
                validations: ruleValidations,
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
