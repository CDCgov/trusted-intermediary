package gov.hhs.cdc.trustedintermediary.etor.ruleengine;

public class ValidationRule implements Rule {
    @Override
    public boolean isValid(String resource) {
        return false;
    }

    @Override
    public boolean appliesTo(String resource) {
        return false;
    }
}
