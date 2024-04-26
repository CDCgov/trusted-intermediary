package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class ValidationRuleEngine implements RuleEngine {
    private String ruleDefinitionsFileName;
    final List<ValidationRule> rules = new ArrayList<>();

    private static final ValidationRuleEngine INSTANCE = new ValidationRuleEngine();

    @Inject RuleLoader ruleLoader;

    public static ValidationRuleEngine getInstance(String ruleDefinitionsFileName) {
        INSTANCE.ruleDefinitionsFileName = ruleDefinitionsFileName;
        return INSTANCE;
    }

    private ValidationRuleEngine() {}

    public void unloadRules() {
        rules.clear();
    }

    public void ensureRulesLoaded() {
        synchronized (this) {
            if (rules.isEmpty()) {
                List<ValidationRule> parsedRules =
                        ruleLoader.loadRules(ruleDefinitionsFileName, new TypeReference<>() {});
                loadRules(parsedRules);
            }
        }
    }

    private synchronized void loadRules(List<ValidationRule> rules) {
        this.rules.addAll(rules);
    }

    public void runRules(FhirResource<?> resource) {
        ensureRulesLoaded();
        for (ValidationRule rule : rules) {
            if (rule.shouldRun(resource)) {
                rule.runRule(resource);
            }
        }
    }
}
