package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class RuleEngine {

    private final String RULES_CONFIG_FILE_NAME = "rule_definitions.json";
    private final List<Rule> rules = new ArrayList<>();

    private static final RuleEngine INSTANCE = new RuleEngine();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    private RuleEngine() {}

    public static RuleEngine getInstance() {
        return INSTANCE;
    }

    public void loadRules() {
        try {
            var rulesDefinitionPath = Path.of("../etor/src/main/resources", RULES_CONFIG_FILE_NAME);
            var loadedRules = ruleLoader.loadRules(rulesDefinitionPath);
            rules.addAll(loadedRules);
        } catch (RuleLoaderException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public void validate(IBaseResource resource) {
        for (Rule rule : rules) {
            if (rule.appliesTo(resource)) {
                if (!rule.isValid(resource)) {
                    logger.logWarning(rule.getWarningMessage());
                }
            }
        }
    }
}
