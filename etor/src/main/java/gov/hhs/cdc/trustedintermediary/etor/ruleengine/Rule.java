package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public interface Rule {
    boolean isValid(String resource);

    boolean appliesTo(String resource);
}
