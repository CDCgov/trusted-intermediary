package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Manages the application of rules loaded from a definitions file using the RuleLoader. */
public class RuleEngine {

    private final String RULES_CONFIG_FILE_NAME = "rule_definitions.json";
    private final List<Rule> rules = new ArrayList<>();
    private boolean rulesLoaded = false;

    private static final RuleEngine INSTANCE = new RuleEngine();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    private RuleEngine() {}

    public static RuleEngine getInstance() {
        return INSTANCE;
    }

    public void ensureRulesLoaded() {
        if (!rulesLoaded) {
            var rulesDefinitionPath = Path.of("../etor/src/main/resources", RULES_CONFIG_FILE_NAME);
            try {
                var loadedRules = ruleLoader.loadRules(rulesDefinitionPath);
                rules.addAll(loadedRules);
                rulesLoaded = true;
            } catch (RuleLoaderException e) {
                logger.logError("Failed to load rules definitions from: " + rulesDefinitionPath, e);
            }
        }
    }

    public void validate(IBaseResource resource) {
        ensureRulesLoaded();
        for (Rule rule : rules) {
            if (rule.appliesTo(resource)) {
                if (!rule.isValid(resource)) {
                    logger.logWarning(rule.getWarningMessage());
                }
            }
        }
    }
}
