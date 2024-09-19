package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine

import ca.uhn.hl7v2.model.Message
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoaderException
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import spock.lang.Specification

class AssertionRuleEngineTest extends Specification {

    def ruleEngine = AssertionRuleEngine.getInstance()
    def mockRuleLoader = Mock(RuleLoader)
    def mockLogger = Mock(Logger)
    def mockRule = Mock(AssertionRule)

    def setup() {
        ruleEngine.unloadRules()

        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, mockRuleLoader)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(AssertionRuleEngine, ruleEngine)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "ensureRulesLoaded happy path"() {
        given:
        mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> [mockRule]

        when:
        ruleEngine.ensureRulesLoaded()

        then:
        1 * mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> [mockRule]
        ruleEngine.assertionRules.size() == 1
    }

    def "ensureRulesLoaded loads rules only once by default"() {
        given:
        mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> [mockRule]

        when:
        ruleEngine.ensureRulesLoaded()
        ruleEngine.ensureRulesLoaded() // Call twice to test if rules are loaded only once

        then:
        1 * mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> [mockRule]
        ruleEngine.assertionRules.size() == 1
    }

    def "ensureRulesLoaded loads rules only once on multiple threads"() {
        given:
        def threadsNum = 10
        def iterations = 4

        when:
        mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> [mockRule]
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
        1 * mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference)
    }

    def "ensureRulesLoaded logs an error if there is an exception loading the rules"() {
        given:
        def exception = new RuleLoaderException("Error loading rules", new Exception())
        mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> {
            mockLogger.logError("Error loading rules", exception)
            return []
        }

        when:
        ruleEngine.runRules(Mock(Message), Mock(Message))

        then:
        1 * mockLogger.logError(_ as String, exception)
    }

    def "runRules logs an error and doesn't run any rules when there's a RuleLoaderException"() {
        given:
        def exception = new RuleLoaderException("Error loading rules", new Exception())
        mockRuleLoader.loadRules(_ as InputStream, _ as TypeReference) >> { throw exception }

        when:
        ruleEngine.runRules(Mock(Message), Mock(Message))

        then:
        1 * mockLogger.logError(_ as String, exception)
    }
}
