package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {
    private final List<Rule> rules = new ArrayList<>();

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public void validate(String resource) {
        for (Rule rule : rules) {
            if (rule.appliesTo(resource)) {
                rule.isValid(resource);
            }
        }
    }
}
