package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleExecutionException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The TransformationRule class extends the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule Rule} class and represents a transformation
 * rule. It implements the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule#runRule(FhirResource) runRule} method to
 * apply a transformation to the FHIR resource.
 */
public class TransformationRule extends Rule<TransformationRuleMethod> {

    //    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    private static final Map<String, CustomFhirTransformation> transformationInstanceCache =
            new ConcurrentHashMap<>();

    /**
     * Do not delete this constructor! It is used for JSON deserialization when loading rules from a
     * file.
     */
    public TransformationRule() {}

    public TransformationRule(
            String ruleName,
            String ruleDescription,
            String ruleMessage,
            List<String> ruleConditions,
            List<TransformationRuleMethod> ruleActions) {
        super(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions);
    }

    @Override
    public void runRule(FhirResource<?> resource) throws RuleExecutionException {
        for (TransformationRuleMethod transformation : this.getRules()) {
            applyTransformation(transformation, resource);
        }
    }

    private void applyTransformation(
            TransformationRuleMethod transformation, FhirResource<?> resource)
            throws RuleExecutionException {
        String name = transformation.name();
        Map<String, String> args = transformation.args();
        logger.logInfo("Applying transformation: ", name);

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
