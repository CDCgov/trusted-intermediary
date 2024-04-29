package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule;
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
        for (TransformationRuleMethod transformation : this.getRules()) {
            String methodName = transformation.name();
            Map<String, String> methodArgs = transformation.args();
            // evaluate methodName(resource, methodArgs)
            // i.e. addEtorProcessingTag(resource, methodArgs) where methodArgs is empty
            // first, need to get methodName from ruleengine/transformation/custom/ using the class
            // name or file name
            // then, need to call the method with the resource and methodArgs
        }
    }
}
