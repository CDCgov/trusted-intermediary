package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule;
import java.lang.reflect.InvocationTargetException;
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

    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

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
    public void runRule(FhirResource<?> resource) {
        this.getRules().forEach((transformation -> applyTransformation(transformation, resource)));
    }

    private void applyTransformation(
            TransformationRuleMethod transformation, FhirResource<?> resource) {
        String name = transformation.name();
        Map<String, String> args = transformation.args();
        logger.logInfo("Applying transformation: ", name);

        try {
            Class<?> clazz = loadClassFromCache(name);
            executeCustomTransformationMethod(clazz, resource, args);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException e) {
            logger.logError("Error invoking method: " + name + ", due to: " + e.getMessage(), e);
        }
    }

    static Class<?> loadClassFromCache(String className) throws RuntimeException {
        return classCache.computeIfAbsent(className, TransformationRule::loadClassByName);
    }

    private static Class<?> loadClassByName(String className) {
        String fullClassName = getFullClassName(className);
        try {
            return Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFullClassName(String className) {
        String packageName =
                "gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom";
        return packageName + "." + className;
    }

    static void executeCustomTransformationMethod(
            Class<?> clazz, FhirResource<?> resource, Map<String, String> args)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
                    IllegalAccessException {
        CustomFhirTransformation transformation =
                (CustomFhirTransformation) clazz.getDeclaredConstructor().newInstance();
        transformation.transform(resource, args);
    }
}
