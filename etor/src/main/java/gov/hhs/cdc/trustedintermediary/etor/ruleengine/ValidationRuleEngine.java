package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;

public class ValidationRuleEngine extends RuleEngine {
    @Inject Logger logger;

    private static final ValidationRuleEngine INSTANCE = new ValidationRuleEngine();

    private ValidationRuleEngine() {
        super(RuleLoader.getInstance(), "validation_definitions.json");
    }

    public static ValidationRuleEngine getInstance() {
        return INSTANCE;
    }
}
