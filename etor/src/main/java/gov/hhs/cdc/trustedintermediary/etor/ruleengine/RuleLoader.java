package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
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

    public List<ValidationRule> loadRules(String ruleStream) throws RuleLoaderException {
        try {
            Map<String, List<ValidationRule>> jsonObj =
                    formatter.convertJsonToObject(ruleStream, new TypeReference<>() {});
            return jsonObj.getOrDefault("rules", Collections.emptyList());
        } catch (FormatterProcessingException e) {
            throw new RuleLoaderException(
                    "Failed to load rules definitions for provided stream", e);
        }
    }
}
