package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

/**
 * The RuleEngine interface defines the structure for a rule engine. Each rule engine has methods to
 * load rules, ensure rules are loaded, and run rules on a resource.
 */
public interface RuleEngine {
    void unloadRules();

    void ensureRulesLoaded() throws RuleLoaderException;

    void runRules(FhirResource<?> resource);

    Rule<?> getRuleByName(String ruleName);
}
