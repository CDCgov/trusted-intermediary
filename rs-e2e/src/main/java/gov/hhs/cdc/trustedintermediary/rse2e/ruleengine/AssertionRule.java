package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import gov.hhs.cdc.trustedintermediary.ruleengine.Rule;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import java.util.List;

/**
 * The AssertionRule class extends the {@link Rule Rule} class and represents an assertion rule. It
 * implements the {@link Rule#runRule(HealthData...) runRule} method to apply an assertion to the
 * HL7 message.
 */
public class AssertionRule extends Rule<String> {

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public AssertionRule() {}

    public AssertionRule(String ruleName, List<String> ruleConditions, List<String> ruleActions) {
        super(ruleName, null, null, ruleConditions, ruleActions);
    }

    @Override
    public final void runRule(HealthData<?>... data) {

        if (data.length != 2) {
            this.logger.logError(
                    "Rule ["
                            + this.getName()
                            + "]: Assertion rules require exactly two data objects to be passed in.");
            return;
        }

        HealthData<?> outputData = data[0];
        HealthData<?> inputData = data[1];

        for (String assertion : this.getRules()) {
            try {
                boolean isValid =
                        this.evaluator.evaluateExpression(assertion, outputData, inputData);
                if (!isValid) {
                    this.logger.logWarning(
                            "Assertion failed for '"
                                    + this.getName()
                                    + "': "
                                    + assertion
                                    + " ("
                                    + outputData.getIdentifier()
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
