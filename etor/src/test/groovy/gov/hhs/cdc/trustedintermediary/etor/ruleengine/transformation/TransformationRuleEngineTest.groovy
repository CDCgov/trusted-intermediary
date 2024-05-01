package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngine
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoader
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class TransformationRuleEngineTest extends Specification {
    def engine = TransformationRuleEngine.getInstance("transformation_definitions.json")
    def mockRuleLoader = Mock(RuleLoader)
    def mockLogger = Mock(Logger)
    def mockRule = Mock(TransformationRule)

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RuleLoader, mockRuleLoader)
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.register(RuleEngine, engine)

        TestApplicationContext.injectRegisteredImplementations()
    }
}
