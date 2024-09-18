package gov.hhs.cdc.trustedintermediary.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;

public class Rule<T> {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);

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

    public boolean shouldRun(HealthData<?> data) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                return data.evaluateCondition(condition);
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

    public void runRule(HealthData<?> data) {
        throw new UnsupportedOperationException("This method must be implemented by subclasses.");
    }

    public void runRule(HealthData<?> outputData, HealthData<?> inputData) {
        throw new UnsupportedOperationException("This method must be implemented by subclasses.");
    }
}
