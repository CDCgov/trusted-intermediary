package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Manages the application of rules loaded from a definitions file using the RuleLoader. */
public class RuleEngine {

    private static final RuleEngine INSTANCE = new RuleEngine();

    final List<Rule> rules = new ArrayList<>();

    @Inject Logger logger;
    @Inject RuleLoader ruleLoader;

    private RuleEngine() {}

    public static RuleEngine getInstance() {
        return INSTANCE;
    }

    public void unloadRules() {
        rules.clear();
    }

    public void ensureRulesLoaded() {
        if (rules.isEmpty()) {
            loadRules();
        }
    }

    private synchronized void loadRules() {
        String fileName = "rule_definitions.json";
        try (InputStream ruleDefinitionStream =
                getClass().getClassLoader().getResourceAsStream(fileName)) {
            assert ruleDefinitionStream != null;
            var ruleStream =
                    new String(ruleDefinitionStream.readAllBytes(), StandardCharsets.UTF_8);
            rules.addAll(ruleLoader.loadRules(ruleStream));
        } catch (IOException | RuleLoaderException e) {
            logger.logError("Failed to load rules definitions from: " + fileName, e);
        }
    }

    public void validate(IBaseResource resource) {
        logger.logDebug("Validating FHIR resource");
        ensureRulesLoaded();
        for (Rule rule : rules) {
            if (rule.appliesTo(resource) && !rule.isValid(resource)) {
                logger.logWarning("Rule violation: " + rule.getViolationMessage());
            }
        }
    }
}
