package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import ca.uhn.hl7v2.model.Message;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7ExpressionEvaluator;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;

/**
 * The AssertionRule class extends the {@link AssertionRule Rule} class and represents a assertion
 * rule. It implements the {@link AssertionRule#runRule(Message, Message) runRule} method to apply a
 * assertion to the FHIR resource.
 */
public class AssertionRule {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);
    protected final HapiFhir fhirEngine = ApplicationContext.getImplementation(HapiFhir.class);

    private String name;
    private List<String> conditions;
    private List<String> rules;

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public AssertionRule() {}

    public AssertionRule(String ruleName, List<String> ruleConditions, List<String> ruleActions) {
        name = ruleName;
        conditions = ruleConditions;
        rules = ruleActions;
    }

    public String getName() {
        return name;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public List<String> getRules() {
        return rules;
    }

    public boolean shouldRun(Message message) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                return HL7ExpressionEvaluator.parseAndEvaluate(
                                        message, null, condition);
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

    public void runRule(Message outputMessage, Message inputMessage) {

        for (String assertion : this.getRules()) {
            try {
                boolean isValid =
                        HL7ExpressionEvaluator.parseAndEvaluate(
                                outputMessage, inputMessage, assertion);
                if (!isValid) {
                    this.logger.logWarning(
                            "Assertion failed for '"
                                    + this.getName()
                                    + "': "
                                    + assertion
                                    + " ("
                                    + outputMessage.getName()
                                    + ")");
                }
            } catch (Exception e) {
                this.logger.logError(
                        "Rule ["
                                + this.getName()
                                + "]: "
                                + "An error occurred while evaluating the assertion: "
                                + assertion,
                        e);
            }
        }
    }
}
