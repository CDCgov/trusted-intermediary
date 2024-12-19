package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.ruleengine.Rule;
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleEngine;
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader;
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoaderException;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** Implements the RuleEngine interface. It represents a rule engine for transformations. */
// Generisize both engines with `Class<>`
public class TransformationRuleEngine<T extends Rule<T>, S extends Rule<T>> implements RuleEngine {
    private String ruleDefinitionsFileName;
    final List<TransformationRule> rules = new ArrayList<>();
    volatile boolean rulesLoaded = false;
    //    private static final TransformationRuleEngine INSTANCE = new TransformationRuleEngine();

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
        rulesLoaded = false;
    }

    @Override
    public void ensureRulesLoaded() throws RuleLoaderException {
        if (!rulesLoaded) {
            synchronized (rules) {
                if (!rulesLoaded) {
                    try (InputStream stream =
                            getClass()
                                    .getClassLoader()
                                    .getResourceAsStream(ruleDefinitionsFileName)) {
                        List<TransformationRule> parsedRules =
                                ruleLoader.loadRules(stream, new TypeReference<>() {});
                        rules.addAll(parsedRules);
                        rulesLoaded = true;

                    } catch (IOException | NullPointerException e) {
                        throw new RuleLoaderException(
                                "File not found: " + ruleDefinitionsFileName,
                                new FileNotFoundException());
                    }
                }
            }
        }
    }

    @Override
    public void runRules(HealthData<?> resource) {
        try {
            ensureRulesLoaded();
        } catch (RuleLoaderException e) {
            logger.logError("Failed to load rules definitions", e);
            return;
        }

        for (TransformationRule rule : rules) {
            if (rule.shouldRun(resource)) {
                rule.runRule((resource));
            }
        }
    }
}
