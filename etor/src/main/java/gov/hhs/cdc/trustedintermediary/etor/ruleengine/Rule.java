package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;

/**
 * The Rule interface defines the structure for a rule in the rule engine. Each rule has a name,
 * description, warning message, conditions, validations, and methods to check if a resource is
 * valid and if the rule applies to a resource.
 */
public interface Rule {
    String getName();

    String getDescription();

    String getMessage();

    List<String> getConditions();

    List<String> getRules();

    boolean shouldRun(FhirResource<?> resource);

    void runRule(FhirResource<?> resource);
}
