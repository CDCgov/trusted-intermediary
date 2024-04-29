package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngine;

public class TransformationRuleEngine implements RuleEngine {
    @Override
    public void unloadRules() {}

    @Override
    public void ensureRulesLoaded() {}

    @Override
    public void runRules(FhirResource<?> resource) {}
}
