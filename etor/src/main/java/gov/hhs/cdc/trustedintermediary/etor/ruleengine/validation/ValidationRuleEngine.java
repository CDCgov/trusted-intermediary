package gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngineImplementation;
import java.util.ArrayList;
import java.util.List;

/** Implements the RuleEngine interface. It represents a rule engine for validations. */
public class ValidationRuleEngine extends RuleEngineImplementation {
    final List<ValidationRule> rules = new ArrayList<>();

    private static final ValidationRuleEngine INSTANCE = new ValidationRuleEngine();

    public static ValidationRuleEngine getInstance(String ruleDefinitionsFileName) {
        INSTANCE.ruleDefinitionsFileName = ruleDefinitionsFileName;
        return INSTANCE;
    }

    //    @Override
    public ValidationRuleEngine getInstance() {
        return INSTANCE;
    }

    private ValidationRuleEngine() {}

    @Override
    public void unloadRules() {
        rules.clear();
    }
}
