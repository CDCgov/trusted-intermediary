package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.ruleengine.Rule;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthDataExpressionEvaluator;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;

/**
 * The AssertionRule class extends the {@link AssertionRule Rule} class and represents a assertion
 * rule. It implements the {@link AssertionRule#runRule(HealthData...) runRule} method to apply a
 * assertion to the FHIR resource.
 */
public class AssertionRule extends Rule<String> {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);
    protected final HealthDataExpressionEvaluator expressionEvaluator =
            ApplicationContext.getImplementation(HealthDataExpressionEvaluator.class);

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
            logger.logError(
                    "Rule ["
                            + this.getName()
                            + "]: Assertion rules require exactly two data objects to be passed in.");
            return;
        }

        HealthData<?> inputData = data[0];
        HealthData<?> outputData = data[1];

        for (String assertion : this.getRules()) {
            try {
                boolean isValid =
                        expressionEvaluator.evaluateExpression(assertion, inputData, outputData);
                if (!isValid) {
                    logger.logWarning(
                            "Assertion failed for '"
                                    + this.getName()
                                    + "': "
                                    + assertion
                                    + " ("
                                    + outputData.getName()
                                    + ")");
                }
            } catch (Exception e) {
                logger.logError(
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
