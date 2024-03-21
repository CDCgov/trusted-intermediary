package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class RuleEngineTest extends Specification {

    def ruleEngine = RuleEngine.getInstance()
    def mockRuleLoader = Mock(RuleLoader)
    def mockLogger = Mock(Logger)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, mockRuleLoader)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(RuleEngine, ruleEngine)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "ensureRulesLoaded happy path"() {
        when:
        ruleEngine.ensureRulesLoaded()

        then:
        1 * mockRuleLoader.loadRules(_ as Path) >> [Mock(Rule)]
        ruleEngine.rules.size() == 1
    }

    def "ensureRulesLoaded loads rules only once"() {
        when:
        ruleEngine.ensureRulesLoaded()
        ruleEngine.ensureRulesLoaded() // Call twice to test if rules are loaded only once

        then:
        1 * mockRuleLoader.loadRules(_ as Path) >> [Mock(Rule)]
    }

    def "ensureRulesLoaded logs an error if there is an exception loading the rules"() {
        given:
        def exception = new RuleLoaderException("Error loading rules")
        mockRuleLoader.loadRules(_ as Path) >> { throw exception }

        when:
        ruleEngine.validate(Mock(Bundle))

        then:
        1 * mockLogger.logError(_ as String, exception)
    }

    def "validate handles logging warning correctly"() {
        given:
        def ruleWarningMessage = "Rule warning message"
        def fhirBundle = Mock(Bundle)
        def invalidRule = Mock(Rule)
        invalidRule.getWarningMessage() >> ruleWarningMessage
        mockRuleLoader.loadRules(_ as Path) >> [invalidRule]

        when:
        invalidRule.appliesTo(fhirBundle) >> true
        invalidRule.isValid(fhirBundle) >> false
        ruleEngine.validate(fhirBundle)

        then:
        1 * mockLogger.logWarning(ruleWarningMessage)

        when:
        invalidRule.appliesTo(fhirBundle) >> true
        invalidRule.isValid(fhirBundle) >> true
        ruleEngine.validate(fhirBundle)

        then:
        0 * mockLogger.logWarning(ruleWarningMessage)

        when:
        invalidRule.appliesTo(fhirBundle) >> false
        invalidRule.isValid(fhirBundle) >> false
        ruleEngine.validate(fhirBundle)

        then:
        0 * mockLogger.logWarning(ruleWarningMessage)
    }
}
