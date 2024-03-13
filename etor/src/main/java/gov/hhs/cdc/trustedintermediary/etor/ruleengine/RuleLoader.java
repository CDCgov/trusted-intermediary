package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class RuleLoader {
    @Inject Formatter formatter;

    List<Rule> loadRules(String configPath) {
        List<Rule> rules = new ArrayList<>();
        RuleConfig ruleConfig;

        try {
            //            configPath = "etor/src/main/resources/rule_defintions.json";
            //            ruleConfig = mapper.readValue(new File(configPath), RuleConfig.class);
            ruleConfig =
                    formatter.convertJsonToObject(
                            Files.readString(Paths.get(configPath)), new TypeReference<>() {});
        } catch (IOException | FormatterProcessingException e) {
            throw new RuntimeException(e);
        }

        for (RuleDefinition ruleDef : ruleConfig.rules) {
            Rule rule = new ValidationRule(ruleDef.name, ruleDef.conditions, ruleDef.validations);
            rules.add(rule);
        }

        return rules;
    }
}
