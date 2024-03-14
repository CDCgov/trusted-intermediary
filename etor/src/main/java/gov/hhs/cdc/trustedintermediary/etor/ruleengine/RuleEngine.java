package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class RuleEngine {

    private final String RULES_CONFIG_FILE_NAME = "rule_definitions.json";
    private static final RuleEngine INSTANCE = new RuleEngine();
    private final List<Rule> rules = new ArrayList<>();

    private RuleEngine() {}

    public static RuleEngine getInstance() {
        return INSTANCE;
    }

    public void loadRules() {
        var fileUrl = getClass().getClassLoader().getResource(RULES_CONFIG_FILE_NAME);
        if (fileUrl == null) {
            throw new IllegalArgumentException("File not found: " + RULES_CONFIG_FILE_NAME);
        }
        try {
            var loadedRules = RuleLoader.getInstance().loadRules(Paths.get(fileUrl.getPath()));
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
                rule.isValid(resource);
            }
        }
    }
}
