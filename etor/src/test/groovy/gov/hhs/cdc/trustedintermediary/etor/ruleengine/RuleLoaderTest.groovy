package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleLoaderTest extends Specification {

    Path tempRulesFilePath

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        tempRulesFilePath = Files.createTempFile("rules", ".json")
        String jsonContent = """
            {
              "rules": [
                {
                  "name": "patientName",
                  "conditions": ["Patient.name.exists()"],
                  "validations": ["Patient.name.where(use='usual').given.exists()"]
                }
              ]
            }
            """
        Files.writeString(tempRulesFilePath, jsonContent)
    }

    def cleanup() {
        Files.deleteIfExists(tempRulesFilePath)
    }

    def "load rules from file"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        List<Rule> rules = RuleLoader.getInstance().loadRules(tempRulesFilePath)

        then:
        rules.size() == 1
        ValidationRule rule = rules.get(0) as ValidationRule
        rule.getName() == "patientName"
        rule.getConditions() == ["Patient.name.exists()"]
        rule.getValidations() == [
            "Patient.name.where(use='usual').given.exists()"
        ]
    }

    def "handle IOException when loading rules from file"() {
        when:
        RuleLoader.getInstance().loadRules("DogCow")

        then:
        thrown(RuleLoaderException)
    }
}
