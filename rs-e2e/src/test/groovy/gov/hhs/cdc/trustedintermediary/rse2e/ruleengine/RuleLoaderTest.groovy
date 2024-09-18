package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.rse2e.HL7ExpressionEvaluator
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleLoaderTest extends Specification {

    String fileContents
    Path tempFile

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HL7ExpressionEvaluator, HL7ExpressionEvaluator.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        tempFile = Files.createTempFile("test_validation_definition", ".json")
    }

    def cleanup(){
        Files.deleteIfExists(tempFile)
    }

    def "load rules from file"() {
        given:
        fileContents = """
        {
            "definitions": [
                {
                    "name": "Example result requirements",
                    "conditions": [
                        "MSH-9.1 = 'R01'",
                        "MSH-6 in ('R797', 'R508')"
                    ],
                    "rules": [
                        "MSH-4 = 'CDPH'",
                        "OBR.count() = 1"
                    ]
                }
            ]
        }
        """
        Files.writeString(tempFile, fileContents)

        when:
        List<AssertionRule> rules = RuleLoader.getInstance().loadRules(Files.newInputStream(tempFile), new TypeReference<Map<String, List<AssertionRule>>>() {})

        then:
        rules.size() == 1
        AssertionRule rule = rules.get(0) as AssertionRule
        rule.getName() == "Example result requirements"
        rule.getConditions() == [
            "MSH-9.1 = 'R01'",
            "MSH-6 in ('R797', 'R508')"
        ]
        rule.getRules() == [
            "MSH-4 = 'CDPH'",
            "OBR.count() = 1"
        ]
    }

    def "handle FormatterProcessingException when loading rules from a non existent file"() {
        given:
        Files.writeString(tempFile, "!K@WJ#8uhy")

        when:
        RuleLoader.getInstance().loadRules(Files.newInputStream(tempFile), new TypeReference<Map<String, List<AssertionRule>>>() {})

        then:
        thrown(RuleLoaderException)
    }
}
