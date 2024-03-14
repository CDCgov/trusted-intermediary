package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;

public class ValidationRule implements Rule {
    private String name;
    private List<String> conditions;
    private List<String> validations;

    public ValidationRule() {}

    public ValidationRule(
            String ruleName, List<String> ruleConditions, List<String> ruleValidations) {
        name = ruleName;
        conditions = ruleConditions;
        validations = ruleValidations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getConditions() {
        return conditions;
    }

    @Override
    public List<String> getValidations() {
        return validations;
    }

    @Override
    public boolean isValid(String resource) {
        return false;
    }

    @Override
    public boolean appliesTo(String resource) {
        return false;
    }
}
