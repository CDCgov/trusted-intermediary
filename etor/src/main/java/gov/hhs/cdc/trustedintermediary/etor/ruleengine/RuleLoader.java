package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/** Manages the loading of rules from a definitions file. */
public class RuleLoader {
    private static final RuleLoader INSTANCE = new RuleLoader();
    @Inject Formatter formatter;
    @Inject Logger logger;

    private RuleLoader() {}

    public static RuleLoader getInstance() {
        return INSTANCE;
    }

    public <T> List<T> loadRules(Path path, TypeReference<Map<String, List<T>>> typeReference) {
        try (InputStream ruleDefinitionStream = Files.newInputStream(path)) {
            assert ruleDefinitionStream != null;
            var rulesString =
                    new String(ruleDefinitionStream.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<T>> jsonObj =
                    formatter.convertJsonToObject(rulesString, typeReference);
            return jsonObj.getOrDefault("definitions", Collections.emptyList());
        } catch (IOException | FormatterProcessingException e) {
            logger.logError("Failed to load rules definitions from: " + path.getFileName(), e);
            return Collections.emptyList();
        }
    }
}
