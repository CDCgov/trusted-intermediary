package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Manages the application of rules loaded from a definitions file using the RuleLoader. */
public class RuleEngine {

    private final Path RULES_DEFINITIONS_PATH =
            Path.of("../etor/src/main/resources/rule_definitions.json");
    private boolean rulesLoaded = false;

    private static final RuleEngine INSTANCE = new RuleEngine();

    final List<Rule> rules = new ArrayList<>();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    private RuleEngine() {}

    public static RuleEngine getInstance() {
        return INSTANCE;
    }

    public void ensureRulesLoaded() {
        ensureRulesLoaded(false);
    }

    public void ensureRulesLoaded(boolean forceUpdate) {
        if (forceUpdate || !rulesLoaded) {
            logger.logDebug("Loading rules definitions from " + RULES_DEFINITIONS_PATH);
            try {
                var loadedRules = ruleLoader.loadRules(RULES_DEFINITIONS_PATH);
                rules.addAll(loadedRules);
                rulesLoaded = true;
            } catch (RuleLoaderException e) {
                logger.logError(
                        "Failed to load rules definitions from: " + RULES_DEFINITIONS_PATH, e);
            }
        }
    }

    public void validate(IBaseResource resource) {
        validate(resource, false);
    }

    public void validate(IBaseResource resource, boolean forceRulesUpdate) {
        logger.logDebug("Validating FHIR resource");
        ensureRulesLoaded(forceRulesUpdate);
        for (Rule rule : rules) {
            if (rule.appliesTo(resource)) {
                if (!rule.isValid(resource)) {
                    logger.logWarning(rule.getWarningMessage());
                }
            }
        }
    }
}
