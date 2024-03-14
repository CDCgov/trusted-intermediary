package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class RuleLoader {

    private static final RuleLoader INSTANCE = new RuleLoader();
    @Inject Formatter formatter;
    @Inject Logger logger;

    private RuleLoader() {}

    public static RuleLoader getInstance() {
        return INSTANCE;
    }

    List<ValidationRule> loadRules(String configPath) throws RuleLoaderException {
        // configPath = "etor/src/main/resources/rule_defintions.json";

        try {
            String fileContent = Files.readString(Paths.get(configPath));
            Map<String, List<ValidationRule>> jsonObj =
                    formatter.convertJsonToObject(fileContent, new TypeReference<>() {});
            return jsonObj.getOrDefault("rules", Collections.emptyList());
        } catch (IOException | FormatterProcessingException e) {
            throw new RuleLoaderException(
                    "Failed to load rules from config path: " + configPath, e);
        }
    }
}
