package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Override
    public void unloadRules() {
        rules.clear();
    }

    @Override
    public void ensureRulesLoaded() {
        if (rules.isEmpty()) {
            synchronized (this) {
                if (rules.isEmpty()) {
                    Path path =
                            Paths.get(
                                    getClass()
                                            .getClassLoader()
                                            .getResource(ruleDefinitionsFileName)
                                            .getPath());

                    List<ValidationRule> parsedRules =
                            ruleLoader.loadRules(path, new TypeReference<>() {});
                    loadRules(parsedRules);
                }
            }
        }
    }

    @Override
    public void runRules(FhirResource<?> resource) {
        ensureRulesLoaded();
        for (ValidationRule rule : rules) {
            if (rule.shouldRun(resource)) {
                rule.runRule(resource);
            }
        }
    }

    private synchronized void loadRules(List<ValidationRule> rules) {
        this.rules.addAll(rules);
    }
}
