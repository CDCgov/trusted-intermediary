package gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation;

import gov.hhs.cdc.trustedintermediary.ruleengine.Rule;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.List;

/**
 * The ValidationRule class extends the {@link gov.hhs.cdc.trustedintermediary.ruleengine.Rule Rule}
 * class and represents a validation rule. It implements the {@link
 * gov.hhs.cdc.trustedintermediary.ruleengine.Rule#runRule(HealthData...) runRule} method to
 * evaluate the validation and log a warning if the validation fails.
 */
public class ValidationRule extends Rule<String> {

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public ValidationRule() {}

    public ValidationRule(
            String ruleName,
            String ruleDescription,
            String ruleMessage,
            List<String> ruleConditions,
            List<String> ruleActions) {
        super(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions);
    }

    @Override
    public void runRule(HealthData<?>... resource) {

        if (resource.length != 1) {
            this.logger.logError(
                    "Rule ["
                            + this.getName()
                            + "]: Validation rules require exactly one resource object to be passed in.");
            return;
        }

        for (String validation : this.getRules()) {
            try {
                boolean isValid = this.evaluator.evaluateExpression(validation, resource[0]);
                if (!isValid) {
                    this.logger.logWarning("Validation failed: " + this.getMessage());
                }
            } catch (Exception e) {
                this.logger.logError(
                        "Rule ["
                                + this.getName()
                                + "]: "
                                + "An error occurred while evaluating the validation: "
                                + validation,
                        e);
            }
        }
    }
}
