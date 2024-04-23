package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;

public class TransformationRule implements Rule {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<String> getConditions() {
        return List.of();
    }

    @Override
    public boolean shouldRun(FhirResource<?> resource) {
        return false;
    }

    @Override
    public void runRule(FhirResource<?> resource) {}
}
