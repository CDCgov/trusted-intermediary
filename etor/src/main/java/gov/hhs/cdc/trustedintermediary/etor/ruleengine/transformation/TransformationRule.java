package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule;
import java.util.List;

/**
 * The TransformationRule class extends the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule Rule} class and represents a transformation
 * rule. It implements the {@link
 * gov.hhs.cdc.trustedintermediary.etor.ruleengine.Rule#runRule(FhirResource) runRule} method to
 * apply a transformation to the FHIR resource.
 */
public class TransformationRule extends Rule {

    public TransformationRule(
            String ruleName,
            String ruleDescription,
            String ruleMessage,
            List<String> ruleConditions,
            List<String> ruleActions) {
        super(ruleName, ruleDescription, ruleMessage, ruleConditions, ruleActions);
    }

    public void runRule(FhirResource<?> resource) {
        for (String transformation : this.getRules()) {
            // todo: implement transformation
        }
    }
}
