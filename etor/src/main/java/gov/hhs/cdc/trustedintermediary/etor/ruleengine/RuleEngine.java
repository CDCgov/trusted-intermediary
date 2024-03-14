package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class RuleEngine {
    private static final RuleEngine INSTANCE = new RuleEngine();
    private final List<Rule> rules = new ArrayList<>();

    private RuleEngine() {
        try {
            var loadedRules = RuleLoader.getInstance().loadRules();
            rules.addAll(loadedRules);
        } catch (RuleLoaderException e) {
            throw new RuntimeException(e);
        }
    }

    public static RuleEngine getInstance() {
        return INSTANCE;
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
