package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import ca.uhn.hl7v2.model.Message;
import gov.hhs.cdc.trustedintermediary.rse2e.external.hapi.HapiHL7Message;
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoader;
import gov.hhs.cdc.trustedintermediary.ruleengine.RuleLoaderException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

/**
 * The AssertionRuleEngine is responsible for loading and running assertion rules against HL7
 * messages.
 */
public class AssertionRuleEngine {
    private static final AssertionRuleEngine INSTANCE = new AssertionRuleEngine();
    final List<AssertionRule> assertionRules = new ArrayList<>();
    volatile boolean rulesLoaded = false;

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    public static AssertionRuleEngine getInstance() {
        return INSTANCE;
    }

    public AssertionRuleEngine() {}

    public void unloadRules() {
        assertionRules.clear();
        rulesLoaded = false;
    }

    public void ensureRulesLoaded() throws RuleLoaderException {
        if (!rulesLoaded) {
            synchronized (assertionRules) {
                if (!rulesLoaded) {
                    String ruleDefinitionsFileName = "assertion_definitions.json";
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
                                "File not found: " + ruleDefinitionsFileName, e);
                    }
                }
            }
        }
    }

    public List<AssertionRule> getRules() {
        return assertionRules;
    }

    public Set<AssertionRule> runRules(Message outputMessage, Message inputMessage) {
        try {
            ensureRulesLoaded();
        } catch (RuleLoaderException e) {
            logger.logError("Failed to load rules definitions", e);
            return Set.of();
        }

        HapiHL7Message outputHapiMessage = new HapiHL7Message(outputMessage);
        HapiHL7Message inputHapiMessage = new HapiHL7Message(inputMessage);

        Set<AssertionRule> runRules = new HashSet<>();
        for (AssertionRule rule : assertionRules) {
            if (rule.shouldRun(outputHapiMessage)) {
                rule.runRule(outputHapiMessage, inputHapiMessage);
                runRules.add(rule);
            }
        }
        return runRules;
    }
}
