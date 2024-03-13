package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;

public class ValidationRule implements Rule {
    String name;
    List<String> validations;

    public ValidationRule(
            String ruleName, List<String> ruleConditions, List<String> ruleValidations) {
        name = ruleName;
        validations = ruleValidations;
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
