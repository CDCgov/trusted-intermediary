package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;

public class ValidationRule implements Rule {
    private String name;
    private List<String> conditions;
    private List<String> validations;
    @Inject HapiFhir fhir;
    @Inject Logger logger;

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
        try {
            var fhirBundle = fhir.parseResource(resource, Bundle.class);
            for (String condition : conditions) {
                // if all conditions are met, then return true
                // otherwise, return false
                return false;
            }
        } catch (FhirParseException e) {
            logger.logError("Failed to parse resource", e);
        }
        return false;
    }
}
