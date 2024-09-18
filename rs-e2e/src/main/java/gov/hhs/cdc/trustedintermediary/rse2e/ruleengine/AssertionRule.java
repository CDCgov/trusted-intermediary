package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7ExpressionEvaluator;
import gov.hhs.cdc.trustedintermediary.ruleengine.Rule;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;

/**
 * The AssertionRule class extends the {@link AssertionRule Rule} class and represents a assertion
 * rule. It implements the {@link AssertionRule#runRule(HealthData, HealthData) runRule} method to
 * apply a assertion to the FHIR resource.
 */
public class AssertionRule extends Rule<String> {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);
    protected final HL7ExpressionEvaluator expressionEvaluator =
            ApplicationContext.getImplementation(HL7ExpressionEvaluator.class);

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public AssertionRule() {}

    public AssertionRule(String ruleName, List<String> ruleConditions, List<String> ruleActions) {
        super(ruleName, null, null, ruleConditions, ruleActions);
    }

    @Override
    public void runRule(HealthData<?> outputData, HealthData<?> inputData) {

        for (String assertion : this.getRules()) {
            try {
                boolean isValid =
                        expressionEvaluator.parseAndEvaluate(
                                outputData.getUnderlyingData(),
                                inputData.getUnderlyingData(),
                                assertion);
                if (!isValid) {
                    this.logger.logWarning(
                            "Assertion failed for '"
                                    + this.getName()
                                    + "': "
                                    + assertion
                                    + " ("
                                    + outputData.getName()
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
