package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.ArrayList;
import java.util.List;

/** Manages the application of rules loaded from a definitions file using the RuleLoader. */
public class RuleEngine {
    final List<Rule> rules = new ArrayList<>();
    RuleLoader ruleLoader;
    String ruleDefinitionsFileName;

    RuleEngine(RuleLoader ruleLoader, String ruleDefinitionsFileName) {
        this.ruleLoader = ruleLoader;
        this.ruleDefinitionsFileName = ruleDefinitionsFileName;
    }

    public void unloadRules() {
        rules.clear();
    }

    public void ensureRulesLoaded() {
        if (rules.isEmpty()) {
            synchronized (this) {
                if (rules.isEmpty()) {
                    var parsedRules = ruleLoader.loadRules(ruleDefinitionsFileName);
                    loadRules(parsedRules);
                }
            }
        }
    }

    private synchronized void loadRules(List<Rule> rules) {
        this.rules.addAll(rules);
    }

    public void runRules(FhirResource<?> resource) {
        ensureRulesLoaded();
        for (Rule rule : rules) {
            if (rule.shouldRun(resource)) {
                rule.runRule(resource);
            }
        }
    }
}
