package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public interface Rule {
    String getName();

    String getDescription();

    String getWarningMessage();

    List<String> getConditions();

    List<String> getValidations();

    boolean isValid(IBaseResource resource);

    boolean appliesTo(IBaseResource resource);
}
