package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngine;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngineImplementation;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoader;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoaderException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** Implements the RuleEngine interface. It represents a rule engine for transformations. */
public class TransformationRuleEngine extends RuleEngineImplementation implements RuleEngine {
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
        // Double-checked locking - needed to protect from excessive sync locks
        if (rules.isEmpty()) {
            synchronized (this) {
                if (rules.isEmpty()) {
                    InputStream resourceStream =
                            getClass()
                                    .getClassLoader()
                                    .getResourceAsStream(ruleDefinitionsFileName);
                    assert resourceStream != null;
                    List<TransformationRule> parsedRules =
                            ruleLoader.loadRules(resourceStream, new TypeReference<>() {});
                    this.rules.addAll(parsedRules);
                }
            }
        }
    }

    protected List<TransformationRule> parseRules() {
        List<TransformationRule> parsedRules =
                ruleLoader.loadRules(resourceStream, new TypeReference<>() {});
    }

    @Override
    public TransformationRule getRuleByName(String ruleName) {
        return (TransformationRule) super.getRuleByName(ruleName);
    }
}
