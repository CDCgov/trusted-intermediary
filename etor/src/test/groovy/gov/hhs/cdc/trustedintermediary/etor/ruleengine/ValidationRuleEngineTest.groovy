package gov.hhs.cdc.trustedintermediary.etor.ruleengine

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

import java.nio.file.Path

class ValidationRuleEngineTest extends Specification {
    def ruleEngine = ValidationRuleEngine.getInstance("validation_definitions.json")
    def mockRuleLoader = Mock(RuleLoader)
    def mockLogger = Mock(Logger)
    def mockRule = Mock(ValidationRule)

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
        given:
        mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> [mockRule]

        when:
        ruleEngine.ensureRulesLoaded()

        then:
        1 * mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> [mockRule]
        ruleEngine.rules.size() == 1
    }

    def "ensureRulesLoaded loads rules only once by default"() {
        given:
        mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> [mockRule]

        when:
        ruleEngine.ensureRulesLoaded()
        ruleEngine.ensureRulesLoaded() // Call twice to test if rules are loaded only once

        then:
        1 * mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> [mockRule]
        ruleEngine.rules.size() == 1
    }

    def "ensureRulesLoaded loads rules only once on multiple threads"() {
        given:
        def threadsNum = 10
        def iterations = 4

        when:
        mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> [mockRule]
        List<Thread> threads = []
        (1..threadsNum).each { threadId ->
            threads.add(new Thread({
                for (int i = 0; i < iterations; i++) {
                    ruleEngine.ensureRulesLoaded()
                }
            }))
        }
        threads*.start()
        threads*.join()

        then:
        1 * mockRuleLoader.loadRules(_ as Path, _ as TypeReference)
    }

    def "ensureRulesLoaded logs an error if there is an exception loading the rules"() {
        given:
        def exception = new RuleLoaderException("Error loading rules", new Exception())
        mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> {
            mockLogger.logError("Error loading rules", exception)
            return []
        }

        when:
        ruleEngine.runRules(Mock(FhirResource))

        then:
        1 * mockLogger.logError(_ as String, exception)
    }

    def "runRules handles logging warning correctly"() {
        given:
        def ruleViolationMessage = "Rule violation message"
        def fullRuleViolationMessage = "Validation failed: " + ruleViolationMessage
        def fhirBundle = Mock(FhirResource)
        def invalidRule = Mock(ValidationRule)
        invalidRule.getMessage() >> ruleViolationMessage
        invalidRule.shouldRun(fhirBundle) >> true
        mockRuleLoader.loadRules(_ as Path, _ as TypeReference) >> [invalidRule]

        when:
        invalidRule.runRule(fhirBundle) >> {
            mockLogger.logWarning(fullRuleViolationMessage)
        }
        ruleEngine.runRules(fhirBundle)

        then:
        1 * mockLogger.logWarning(fullRuleViolationMessage)

        when:
        invalidRule.runRule(fhirBundle) >> null
        invalidRule.shouldRun(fhirBundle) >> true
        ruleEngine.runRules(fhirBundle)

        then:
        0 * mockLogger.logWarning(fullRuleViolationMessage)

        when:
        invalidRule.shouldRun(fhirBundle) >> false
        ruleEngine.runRules(fhirBundle)

        then:
        0 * mockLogger.logWarning(fullRuleViolationMessage)
    }
}
