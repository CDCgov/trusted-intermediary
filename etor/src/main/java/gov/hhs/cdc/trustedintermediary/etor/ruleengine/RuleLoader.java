package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/** Manages the loading of rules from a definitions file. */
public class RuleLoader {
    List<String> ruleFileNames;
    private static final RuleLoader INSTANCE = new RuleLoader();
    @Inject Formatter formatter;
    @Inject Logger logger;

    private RuleLoader() {}

    public static RuleLoader getInstance() {
        return INSTANCE;
    }

    public void loadDefinitions(String fileName) {
        ruleFileNames.add(fileName);
    }

    public List<Rule> loadRules() {
        List<Rule> rules = new ArrayList<>();
        for (String fileName : ruleFileNames) {
            try (InputStream ruleDefinitionStream =
                    getClass().getClassLoader().getResourceAsStream(fileName)) {
                assert ruleDefinitionStream != null;
                var rulesString =
                        new String(ruleDefinitionStream.readAllBytes(), StandardCharsets.UTF_8);
                Map<String, List<Rule>> jsonObj =
                        formatter.convertJsonToObject(rulesString, new TypeReference<>() {});
                rules.addAll(jsonObj.getOrDefault("definitions", Collections.emptyList()));

            } catch (IOException | FormatterProcessingException e) {
                logger.logError("Failed to load rules definitions from: " + fileName, e);
            }
        }
        return rules;
    }
}
