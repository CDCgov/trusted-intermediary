package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public interface IRuleEngine {

    void unloadRules();

    void ensureRulesLoaded();

    void runRules(FhirResource<?> resource);
}
