package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

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

    public TransformationRule(
            String ruleName,
            String ruleDescription,
            String ruleMessage,
            List<String> ruleConditions,
            List<TransformationRuleMethod> ruleActions) {
        super(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions);
    }

    public void runRule(FhirResource<?> resource) {
        Path customTransformatiosPath =
                Path.of(
                        "etor/src/main/java/gov/hhs/cdc/trustedintermediary/etor/ruleengine/transformation/custom/");
        File[] customTransformationFiles = customTransformatiosPath.toFile().listFiles();

        if (customTransformationFiles == null) {
            logger.logInfo("No custom transformation files found.");
            return;
        }

        for (TransformationRuleMethod transformation : this.getRules()) {
            String methodName = transformation.name();
            Map<String, String> methodArgs = transformation.args();

            for (File file : customTransformationFiles) {
                if (file.isFile() && (file.getName().endsWith(".class"))) {
                    String className = file.getName().replace(".class", "");
                    if (className.equalsIgnoreCase(methodName)) {
                        try {
                            Class<?> clazz = Class.forName(className);
                            Method method =
                                    clazz.getDeclaredMethod(
                                            "transform", FhirResource.class, Map.class);
                            method.invoke(
                                    clazz.getDeclaredConstructor().newInstance(),
                                    resource,
                                    methodArgs);
                            return;
                        } catch (ClassNotFoundException
                                | NoSuchMethodException
                                | IllegalAccessException
                                | InvocationTargetException
                                | InstantiationException e) {
                            logger.logError("Error invoking method: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
