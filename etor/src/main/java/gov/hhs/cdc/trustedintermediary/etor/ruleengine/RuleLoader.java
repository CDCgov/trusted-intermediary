package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class RuleLoader {

    private final String RULES_CONFIG_FILE_NAME = "rule_definitions.json";
    private static final RuleLoader INSTANCE = new RuleLoader();
    @Inject Formatter formatter;
    @Inject Logger logger;

    private RuleLoader() {}

    public static RuleLoader getInstance() {
        return INSTANCE;
    }

    List<ValidationRule> loadRules() throws RuleLoaderException {
        var fileUrl = getClass().getClassLoader().getResource(RULES_CONFIG_FILE_NAME);
        if (fileUrl == null) {
            throw new IllegalArgumentException("File not found: " + RULES_CONFIG_FILE_NAME);
        }
        return loadRules(Paths.get(fileUrl.getPath()));
    }

    List<ValidationRule> loadRules(Path configPath) throws RuleLoaderException {
        try {
            String fileContent = Files.readString(configPath);
            Map<String, List<ValidationRule>> jsonObj =
                    formatter.convertJsonToObject(fileContent, new TypeReference<>() {});
            return jsonObj.getOrDefault("rules", Collections.emptyList());
        } catch (IOException | FormatterProcessingException e) {
            throw new RuleLoaderException(
                    "Failed to load rules from config path: " + configPath, e);
        }
    }
}
