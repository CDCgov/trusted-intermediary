package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public abstract class RuleEngineImplementation implements RuleEngine {
    protected String ruleDefinitionsFileName;

    private List<Rule<String>> rules = new ArrayList<>();

    @Inject Logger logger;

    @Inject RuleLoader ruleLoader;

    protected RuleEngineImplementation() {}

    @Override
    public abstract void unloadRules();

    @Override
    public void ensureRulesLoaded() throws RuleLoaderException {
        // Double-checked locking - needed to protect from excessive sync locks
        if (rules.isEmpty()) {
            synchronized (this) {
                if (rules.isEmpty()) {
                    InputStream resourceStream =
                            getClass()
                                    .getClassLoader()
                                    .getResourceAsStream(ruleDefinitionsFileName);
                    assert resourceStream != null;
                    List<Rule<String>> parsedRules =
                            ruleLoader.loadRules(resourceStream, new TypeReference<>() {});
                    this.rules.addAll(parsedRules);
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

        rules.forEach(
                rule -> {
                    if (rule.shouldRun(resource)) {
                        rule.runRule((resource));
                    }
                });
    }

    @Override
    public Rule<?> getRuleByName(String ruleName) {
        return rules.stream()
                .filter(rule -> rule.getName().equals(ruleName))
                .findFirst()
                .orElse(null);
    }
}
