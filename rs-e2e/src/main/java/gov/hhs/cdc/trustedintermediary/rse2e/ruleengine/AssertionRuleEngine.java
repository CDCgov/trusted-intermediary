package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import ca.uhn.hl7v2.model.Message;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** Implements the RuleEngine interface. It represents a rule engine for transformations. */
public class AssertionRuleEngine {
    private final String ruleDefinitionsFileName = "assertion_definitions.json";
    final List<AssertionRule> assertionRules = new ArrayList<>();
    volatile boolean rulesLoaded = false;
    private static final AssertionRuleEngine INSTANCE = new AssertionRuleEngine();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    private AssertionRuleEngine() {}

    public void unloadRules() {
        assertionRules.clear();
        rulesLoaded = false;
    }

    public void ensureRulesLoaded() throws RuleLoaderException {
        if (!rulesLoaded) {
            synchronized (assertionRules) {
                if (!rulesLoaded) {
                    try (InputStream stream =
                            getClass()
                                    .getClassLoader()
                                    .getResourceAsStream(ruleDefinitionsFileName)) {
                        List<AssertionRule> parsedRules =
                                ruleLoader.loadRules(stream, new TypeReference<>() {});
                        assertionRules.addAll(parsedRules);
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

    public void runRules(Message outputMessage, Message inputMessage) {
        try {
            ensureRulesLoaded();
        } catch (RuleLoaderException e) {
            logger.logError("Failed to load rules definitions", e);
            return;
        }

        for (AssertionRule rule : assertionRules) {
            if (rule.shouldRun(outputMessage)) {
                rule.runRule(outputMessage, inputMessage);
            }
        }
    }
}
