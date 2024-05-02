package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The TransformationRule class extends the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule Rule} class and represents a transformation
 * rule. It implements the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule#runRule(FhirResource) runRule} method to
 * apply a transformation to the FHIR resource.
 */
public class TransformationRule extends Rule<TransformationRuleMethod> {

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

    public void runRule(FhirResource<?> resource) {

        this.getRules().forEach((transformation -> applyTransformation(transformation, resource)));
    }

    private void applyTransformation(
            TransformationRuleMethod transformation, FhirResource<?> resource) {
        String name = transformation.name();
        Map<String, String> args = transformation.args();
        logger.logInfo("Applying transformation: ", name);

        try {
            Class<?> clazz = loadCustomTransformationClassFromFile(name);
            Method method = clazz.getDeclaredMethod("transform", FhirResource.class, Map.class);
            method.invoke(clazz.getDeclaredConstructor().newInstance(), resource, args);
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException e) {
            logger.logError("Error invoking method: " + name, e);
        }
    }

    private static Class<?> loadCustomTransformationClassFromFile(String className)
            throws ClassNotFoundException {
        String customPackageName =
                "gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom";
        Path rootPath = ApplicationContext.getRootPath();
        Path customTransformationPath =
                rootPath.resolve(
                        "etor/src/main/java/gov/hhs/cdc/trustedintermediary/etor/ruleengine/transformation/custom/");
        File[] customTransformationFiles = customTransformationPath.toFile().listFiles();
        assert customTransformationFiles != null;

        for (File file : customTransformationFiles) {
            String fileName = file.getName().replace(".java", "");
            if (file.isFile()
                    && (file.getName().endsWith(".java"))
                    && (fileName.equalsIgnoreCase(className))) {
                return Class.forName(customPackageName + "." + className);
            }
        }

        throw new ClassNotFoundException("No custom transformation file found for " + className);
    }
}
