package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class RuleLoaderTest extends Specification {

    String fileContents

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, RuleLoader.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        fileContents = """
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
    }

    def "load rules from file"() {
        given:
        TestApplicationContext.register(HapiFhir, Mock(HapiFhir))
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        when:
        List<Rule> rules = RuleLoader.getInstance().loadRules(fileContents)

        then:
        rules.size() == 1
        ValidationRule rule = rules.get(0) as ValidationRule
        rule.getName() == "patientName"
        rule.getConditions() == ["Patient.name.exists()"]
        rule.getValidations() == [
            "Patient.name.where(use='usual').given.exists()"
        ]
    }

    def "handle FormatterProcessingException when loading rules from file"() {
        when:
        RuleLoader.getInstance().loadRules("!K@WJ#8uhy")

        then:
        thrown(RuleLoaderException)
    }
}
