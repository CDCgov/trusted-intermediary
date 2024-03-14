package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

import java.util.List;

public interface Rule {
    String getName();

    List<String> getConditions();

    List<String> getValidations();

    boolean isValid(String resource);

    boolean appliesTo(String resource);
}
