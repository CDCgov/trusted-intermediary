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

    def "ensureRulesLoaded loads rules only once on multiple threads"() {
        given:
        def threadsNum = 10
        def iterations = 4

        when:
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
        1 * mockRuleLoader.loadRules(_ as String) >> [Mock(Rule)]
    }

    def "ensureRulesLoaded logs an error if there is an exception loading the rules"() {
        given:
        def exception = new RuleLoaderException("Error loading rules", new Exception())
        mockRuleLoader.loadRules(_ as String) >> { throw exception }

        when:
        ruleEngine.runRules(Mock(FhirResource))

        then:
        1 * mockLogger.logError(_ as String, exception)
    }

    def 'runRules handles logging warning correctly'() {
        given:
        def ruleViolationMessage = "Rule violation message"
        def fullRuleViolationMessage = "Rule violation: " + ruleViolationMessage
        def fhirBundle = Mock(FhirResource)
        def invalidRule = Mock(Rule)
        invalidRule.getViolationMessage() >> ruleViolationMessage
        mockRuleLoader.loadRules(_ as String) >> [invalidRule]

        when:
        invalidRule.shouldRun(fhirBundle) >> true
        invalidRule.isValid(fhirBundle) >> false
        ruleEngine.runRules(fhirBundle)

        then:
        1 * mockLogger.logWarning(fullRuleViolationMessage)

        when:
        invalidRule.shouldRun(fhirBundle) >> true
        invalidRule.isValid(fhirBundle) >> true
        ruleEngine.runRules(fhirBundle)

        then:
        0 * mockLogger.logWarning(fullRuleViolationMessage)

        when:
        invalidRule.shouldRun(fhirBundle) >> false
        invalidRule.isValid(fhirBundle) >> false
        ruleEngine.runRules(fhirBundle)

        then:
        0 * mockLogger.logWarning(fullRuleViolationMessage)
    }
}
