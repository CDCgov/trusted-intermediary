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

    private static final RuleEngine INSTANCE = new RuleEngine();

    final List<Rule> rules = new ArrayList<>();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    private RuleEngine() {}

    public static RuleEngine getInstance() {
        return INSTANCE;
    }

    public void unloadRules() {
        rules.clear();
    }

    public void ensureRulesLoaded() {
        if (!rules.isEmpty()) {
            return;
        }
        logger.logInfo("Loading rules definitions from " + RULES_DEFINITIONS_PATH);
        try {
            var loadedRules = ruleLoader.loadRules(RULES_DEFINITIONS_PATH);
            rules.addAll(loadedRules);
        } catch (RuleLoaderException e) {
            logger.logError("Failed to load rules definitions from: " + RULES_DEFINITIONS_PATH, e);
        }
    }

    public void validate(IBaseResource resource) {
        logger.logDebug("Validating FHIR resource");
        ensureRulesLoaded();
        for (Rule rule : rules) {
            if (rule.appliesTo(resource) && !rule.isValid(resource)) {
                logger.logWarning(rule.getWarningMessage());
            }
        }
    }
}
