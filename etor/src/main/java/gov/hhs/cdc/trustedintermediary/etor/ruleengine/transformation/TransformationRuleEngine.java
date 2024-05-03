package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngine;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoader;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoaderException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** Implements the RuleEngine interface. It represents a rule engine for transformations. */
public class TransformationRuleEngine implements RuleEngine {
    private String ruleDefinitionsFileName;
    final List<TransformationRule> rules = new ArrayList<>();

    private static final TransformationRuleEngine INSTANCE = new TransformationRuleEngine();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    public static TransformationRuleEngine getInstance(String ruleDefinitionsFileName) {
        INSTANCE.ruleDefinitionsFileName = ruleDefinitionsFileName;
        return INSTANCE;
    }

    private TransformationRuleEngine() {}

    @Override
    public void unloadRules() {
        rules.clear();
    }

    @Override
    public void ensureRulesLoaded() throws RuleLoaderException {
        synchronized (this) {
            if (rules.isEmpty()) {
                String path = ruleDefinitionsFileName;
                try (InputStream resourceStream =
                        getClass().getClassLoader().getResourceAsStream(path)) {
                    if (resourceStream == null) {
                        throw new RuleLoaderException("No resource found at " + path);
                    }
                    List<TransformationRule> parsedRules =
                            ruleLoader.loadRules(resourceStream, new TypeReference<>() {});
                    this.rules.addAll(parsedRules);
                } catch (IOException e) {
                    throw new RuleLoaderException("Failed to load rules from " + path, e);
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
                    try {
                        if (rule.shouldRun(resource)) {
                            rule.runRule((resource));
                        }
                    } catch (Exception e) {
                        logger.logError("Error executing rule: " + rule.getName(), e);
                    }
                });
    }

    @Override
    public TransformationRule getRuleByName(String ruleName) {
        return rules.stream()
                .filter(rule -> rule.getName().equals(ruleName))
                .findFirst()
                .orElse(null);
    }
}
