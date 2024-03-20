package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class ValidationRule implements Rule {

    private static final ValidationRule INSTANCE = new ValidationRule();
    private String name;
    private String description;
    private String warningMessage;
    private List<String> conditions;
    private List<String> validations;

    private ValidationRule() {}

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

    @Inject HapiFhir fhirEngine;
    @Inject Logger logger;

    public static ValidationRule getInstance() {
        return INSTANCE;
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
