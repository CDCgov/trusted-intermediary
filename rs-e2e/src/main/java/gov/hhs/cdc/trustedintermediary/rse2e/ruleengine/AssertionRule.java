package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The AssertionRule class extends the {@link AssertionRule Rule} class and represents a assertion
 * rule. It implements the {@link AssertionRule#runRule(HL7Message) runRule} method to apply a
 * assertion to the FHIR resource.
 */
public class AssertionRule {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);
    protected final HapiFhir fhirEngine = ApplicationContext.getImplementation(HapiFhir.class);

    private static final Map<String, CustomHL7Assertion> assertionInstanceCache =
            new ConcurrentHashMap<>();

    private String name;
    private String description;
    private String message;
    private List<String> conditions;
    private List<AssertionRuleMethod> rules;

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public AssertionRule() {}

    public AssertionRule(
            String ruleName,
            String ruleDescription,
            String ruleMessage,
            List<String> ruleConditions,
            List<AssertionRuleMethod> ruleActions) {
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

    public List<AssertionRuleMethod> getRules() {
        return rules;
    }

    public boolean shouldRun(HL7Message<?> resource) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                // TODO: Implement the evaluateCondition method for HL7
                                //                                return
                                // fhirEngine.evaluateCondition(resource.getMessage(), condition);
                                return false;
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

    public void runRule(HL7Message<?> resource) {
        for (AssertionRuleMethod assertion : this.getRules()) {
            try {
                applyAssertion(assertion, resource);
            } catch (RuntimeException e) {
                logger.logError("Error applying assertion: " + assertion.name(), e);
            }
        }
    }

    private void applyAssertion(AssertionRuleMethod assertion, HL7Message<?> resource) {
        String name = assertion.name();
        Map<String, ArrayList<?>> args = assertion.args();
        logger.logInfo("Applying assertion: " + name);

        CustomHL7Assertion assertionInstance = getAssertionInstance(name);
        assertionInstance.transform(resource, args);
    }

    static CustomHL7Assertion getAssertionInstance(String name) throws RuntimeException {
        return assertionInstanceCache.computeIfAbsent(name, AssertionRule::createAssertionInstance);
    }

    private static CustomHL7Assertion createAssertionInstance(String assertionName) {
        String fullClassName = getFullClassName(assertionName);
        try {
            Class<?> clazz = Class.forName(fullClassName);
            return (CustomHL7Assertion) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error creating assertion instance for: " + assertionName, e);
        }
    }

    private static String getFullClassName(String className) {
        String packageName = "gov.hhs.cdc.trustedintermediary.rse2e.ruleengine.custom";
        return packageName + "." + className;
    }
}
