package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** Manages the application of rules loaded from a definitions file using the RuleLoader. */
public class RuleEngine implements IRuleEngine {
    private Class<?> ruleClass;
    private String ruleDefinitionsFileName;
    final List<Rule> rules = new ArrayList<>();

    private static final RuleEngine INSTANCE = new RuleEngine();

    @Inject RuleLoader ruleLoader;

    public static RuleEngine getInstance(String ruleDefinitionsFileName, Class<?> ruleClass) {
        INSTANCE.ruleDefinitionsFileName = ruleDefinitionsFileName;
        INSTANCE.ruleClass = ruleClass;
        return INSTANCE;
    }

    private RuleEngine() {}

    public void unloadRules() {
        rules.clear();
    }

    public void ensureRulesLoaded() {
        synchronized (this) {
            if (rules.isEmpty()) {
                List<Rule> parsedRules = ruleLoader.loadRules(ruleDefinitionsFileName, ruleClass);
                loadRules(parsedRules);
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
