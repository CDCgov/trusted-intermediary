package gov.hhs.cdc.trustedintermediary.etor.ruleengine

class RuleEngineHelper {

    static <T extends Rule> T getRuleByName(List<T> rules, String ruleName) {
        return rules.stream()
                .filter({ rule -> rule.getName() == ruleName })
                .findFirst()
                .orElse(null)
    }
}
