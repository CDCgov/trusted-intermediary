package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * The Rule interface defines the structure for a rule in the rule engine. Each rule has a name,
 * description, warning message, conditions, validations, and methods to check if a resource is
 * valid and if the rule applies to a resource.
 */
public interface Rule {
    String getName();

    String getDescription();

    /**
     * Descriptive message when there's a rule violation Note: When implementing this method, make
     * sure that no PII or PHI is included in the message!
     */
    String getViolationMessage();

    List<String> getConditions();

    List<String> getValidations();

    boolean isValid(IBaseResource resource);

    boolean appliesTo(IBaseResource resource);
}
