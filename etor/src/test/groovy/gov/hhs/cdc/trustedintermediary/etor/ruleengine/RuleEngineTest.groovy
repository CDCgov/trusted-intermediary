package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class RuleEngineTest extends Specification {
    def ruleEngine = RuleEngine.getInstance()
    def mockRuleLoader = Mock(RuleLoader)
    def mockLogger = Mock(Logger)

    def setup() {
        ruleEngine.unloadRules()

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
        1 * mockRuleLoader.loadRules(_ as String) >> [Mock(Rule)]
        ruleEngine.rules.size() == 1
    }

    def "ensureRulesLoaded loads rules only once by default"() {
        when:
        ruleEngine.ensureRulesLoaded()
        ruleEngine.ensureRulesLoaded() // Call twice to test if rules are loaded only once

        then:
        1 * mockRuleLoader.loadRules(_ as String) >> [Mock(Rule)]
    }

    def "ensureRulesLoaded logs an error if there is an exception loading the rules"() {
        given:
        def exception = new RuleLoaderException("Error loading rules", new Exception())
        mockRuleLoader.loadRules(_ as String) >> { throw exception }

        when:
        ruleEngine.validate(Mock(FhirResource))

        then:
        1 * mockLogger.logError(_ as String, exception)
    }

    def "validate handles logging warning correctly"() {
        given:
        def ruleViolationMessage = "Rule violation message"
        def fullRuleViolationMessage = "Rule violation: " + ruleViolationMessage
        def fhirBundle = Mock(FhirResource)
        def invalidRule = Mock(Rule)
        invalidRule.getViolationMessage() >> ruleViolationMessage
        mockRuleLoader.loadRules(_ as String) >> [invalidRule]

        when:
        invalidRule.appliesTo(fhirBundle) >> true
        invalidRule.isValid(fhirBundle) >> false
        ruleEngine.validate(fhirBundle)

        then:
        1 * mockLogger.logWarning(fullRuleViolationMessage)

        when:
        invalidRule.appliesTo(fhirBundle) >> true
        invalidRule.isValid(fhirBundle) >> true
        ruleEngine.validate(fhirBundle)

        then:
        0 * mockLogger.logWarning(fullRuleViolationMessage)

        when:
        invalidRule.appliesTo(fhirBundle) >> false
        invalidRule.isValid(fhirBundle) >> false
        ruleEngine.validate(fhirBundle)

        then:
        0 * mockLogger.logWarning(fullRuleViolationMessage)
    }
}
