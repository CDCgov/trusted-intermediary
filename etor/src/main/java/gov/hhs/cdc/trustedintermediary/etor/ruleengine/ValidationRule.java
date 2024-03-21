package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

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
    private String warningMessage;
    private List<String> conditions;
    private List<String> validations;

    public ValidationRule() {}

    public ValidationRule(
            String ruleName,
            String ruleDescription,
            String ruleWarningMessage,
            List<String> ruleConditions,
            List<String> ruleValidations) {
        name = ruleName;
        description = ruleDescription;
        warningMessage = ruleWarningMessage;
        conditions = ruleConditions;
        validations = ruleValidations;
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
    public String getWarningMessage() {
        return warningMessage;
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
    public boolean isValid(IBaseResource resource) {
        return validations.stream()
                .allMatch(
                        validation -> {
                            try {
                                return fhirEngine.evaluateCondition(resource, validation);
                            } catch (Exception e) {
                                logger.logError(
                                        "An error occurred while evaluating the validation: "
                                                + validation,
                                        e);
                                return false;
                            }
                        });
    }

    @Override
    public boolean appliesTo(IBaseResource resource) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                return fhirEngine.evaluateCondition(resource, condition);
                            } catch (Exception e) {
                                logger.logError(
                                        "An error occurred while evaluating the condition: "
                                                + condition,
                                        e);
                                return false;
                            }
                        });
    }
}
