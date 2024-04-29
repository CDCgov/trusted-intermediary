package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** Implements the RuleEngine interface. It represents a rule engine for validations. */
public class ValidationRuleEngine implements RuleEngine {
    private String ruleDefinitionsFileName;
    final List<ValidationRule> rules = new ArrayList<>();

    private static final ValidationRuleEngine INSTANCE = new ValidationRuleEngine();

    @Inject Logger logger;
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
    public void ensureRulesLoaded() throws RuleLoaderException {
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
        try {
            ensureRulesLoaded();
        } catch (RuleLoaderException e) {
            logger.logError("Failed to load rules definitions", e);
            return;
        }
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
