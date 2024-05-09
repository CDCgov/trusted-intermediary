package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;

/**
 * Represents a rule that can be run on a FHIR resource. Each rule has a name, description, logging
 * message, conditions to determine if the rule should run, and actions to run in case the condition
 * is met.
 */
public class Rule<T> {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);
    protected final HapiFhir fhirEngine = ApplicationContext.getImplementation(HapiFhir.class);
    private String name;
    private String description;
    private String message;
    private List<String> conditions;
    private List<T> rules;

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public Rule() {}

    public Rule(
            String ruleName,
            String ruleDescription,
            String ruleMessage,
            List<String> ruleConditions,
            List<T> ruleActions) {
        name = ruleName;
        description = ruleDescription;
        message = ruleMessage;
        conditions = ruleConditions;
        rules = ruleActions;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public List<T> getRules() {
        return rules;
    }

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

    public void runRule(FhirResource<?> resource) throws RuleExecutionException {
        throw new UnsupportedOperationException("This method must be implemented by subclasses.");
    }
}
