package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;

/**
 * Implements the Rule interface. It represents a rule with a name, description, warning message,
 * conditions, and validations. It uses the HapiFhir engine to evaluate the conditions and
 * validations.
 */
public class ValidationRule implements Rule {

    private final Logger logger = ApplicationContext.getImplementation(Logger.class);
    private final HapiFhir fhirEngine = ApplicationContext.getImplementation(HapiFhir.class);
    private String name;
    private String description;
    private String message;
    private List<String> conditions;
    private List<String> rules;

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
        name = ruleName;
        description = ruleDescription;
        message = ruleMessage;
        conditions = ruleConditions;
        rules = ruleActions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<String> getConditions() {
        return conditions;
    }

    @Override
    public List<String> getRules() {
        return rules;
    }

    @Override
    public boolean shouldRun(FhirResource<?> resource) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                return fhirEngine.evaluateCondition(
                                        resource.getUnderlyingResource(), condition);
                            } catch (Exception e) {
                                logger.logError(
                                        "Rule ["
                                                + name
                                                + "]: "
                                                + "An error occurred while evaluating the condition: "
                                                + condition,
                                        e);
                                return false;
                            }
                        });
    }

    @Override
    public void runRule(FhirResource<?> resource) {
        for (String validation : rules) {
            try {
                boolean isValid =
                        fhirEngine.evaluateCondition(resource.getUnderlyingResource(), validation);
                if (!isValid) {
                    logger.logWarning("Validation failed: " + message);
                }
            } catch (Exception e) {
                logger.logError(
                        "Rule ["
                                + name
                                + "]: "
                                + "An error occurred while evaluating the validation: "
                                + validation,
                        e);
            }
        }
    }
}
