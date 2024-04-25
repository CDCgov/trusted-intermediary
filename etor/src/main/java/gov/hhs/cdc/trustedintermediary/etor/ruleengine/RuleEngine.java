package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.ArrayList;
import java.util.List;

/** Manages the application of rules loaded from a definitions file using the RuleLoader. */
public class RuleEngine<T extends Rule> {
    final List<T> rules = new ArrayList<>();
    private final RuleLoader ruleLoader;
    private final String ruleDefinitionsFileName;
    private final Class<T> ruleClass;

    RuleEngine(RuleLoader ruleLoader, String ruleDefinitionsFileName, Class<T> ruleClass) {
        this.ruleLoader = ruleLoader;
        this.ruleDefinitionsFileName = ruleDefinitionsFileName;
        this.ruleClass = ruleClass;
    }

    public void unloadRules() {
        rules.clear();
    }

    public void ensureRulesLoaded() {
        if (rules.isEmpty()) {
            synchronized (this) {
                if (rules.isEmpty()) {
                    List<T> parsedRules = ruleLoader.loadRules(ruleDefinitionsFileName, ruleClass);
                    loadRules(parsedRules);
                }
            }
        }
    }

    private synchronized void loadRules(List<T> rules) {
        this.rules.addAll(rules);
    }

    public void runRules(FhirResource<?> resource) {
        ensureRulesLoaded();
        for (T rule : rules) {
            if (rule.shouldRun(resource)) {
                rule.runRule(resource);
            }
        }
    }
}
