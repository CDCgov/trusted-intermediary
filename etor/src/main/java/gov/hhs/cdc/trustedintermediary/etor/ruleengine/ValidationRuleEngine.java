package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public class ValidationRuleEngine extends RuleEngine<ValidationRule> {
    private static final ValidationRuleEngine INSTANCE = new ValidationRuleEngine();

    private ValidationRuleEngine() {
        super(RuleLoader.getInstance(), "validation_definitions.json", ValidationRule.class);
    }

    public static ValidationRuleEngine getInstance() {
        return INSTANCE;
    }
}
