package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The TransformationRule class extends the {@link Rule Rule} class and represents a transformation
 * rule. It implements the {@link Rule#runRule(HL7Message) runRule} method to apply a transformation
 * to the FHIR resource.
 */
public class Rule<T> {

    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);
    protected final HapiFhir fhirEngine = ApplicationContext.getImplementation(HapiFhir.class);

    private static final Map<String, CustomFhirTransformation> transformationInstanceCache =
            new ConcurrentHashMap<>();

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

    public boolean shouldRun(HL7Message<?> resource) {
        return conditions.stream()
                .allMatch(
                        condition -> {
                            try {
                                return fhirEngine.evaluateCondition(
                                        resource.getMessage(), condition);
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
        for (TransformationRuleMethod transformation : this.getRules()) {
            try {
                applyTransformation(transformation, resource);
            } catch (RuntimeException e) {
                logger.logError("Error applying transformation: " + transformation.name(), e);
            }
        }
    }

    private void applyTransformation(
            TransformationRuleMethod transformation, HL7Message<?> resource) {
        String name = transformation.name();
        Map<String, String> args = transformation.args();
        logger.logInfo("Applying transformation: " + name);

        CustomFhirTransformation transformationInstance = getTransformationInstance(name);
        transformationInstance.transform(resource, args);
    }

    static CustomFhirTransformation getTransformationInstance(String name) throws RuntimeException {
        return transformationInstanceCache.computeIfAbsent(
                name, TransformationRule::createTransformationInstance);
    }

    private static CustomFhirTransformation createTransformationInstance(
            String transformationName) {
        String fullClassName = getFullClassName(transformationName);
        try {
            Class<?> clazz = Class.forName(fullClassName);
            return (CustomFhirTransformation) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error creating transformation instance for: " + transformationName, e);
        }
    }

    private static String getFullClassName(String className) {
        String packageName =
                "gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom";
        return packageName + "." + className;
    }
}
