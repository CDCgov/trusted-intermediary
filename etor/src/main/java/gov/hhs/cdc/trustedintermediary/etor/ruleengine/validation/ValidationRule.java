package gov.hhs.cdc.trustedintermediary.etor.ruleengine.validation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule;
import java.util.List;

/**
 * The ValidationRule class extends the {@link gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule
 * Rule} class and represents a validation rule. It implements the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule#runRule(FhirResource) runRule} method to
 * evaluate the validation and log a warning if the validation fails.
 */
public class ValidationRule extends Rule {

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
    public void runRule(FhirResource<?> resource) {
        for (String validation : this.getRules()) {
            try {
                boolean isValid =
                        this.fhirEngine.evaluateCondition(
                                resource.getUnderlyingResource(), validation);
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
