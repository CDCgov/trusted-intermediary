package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData


class IllegalAccessExceptionMockClass {

    private IllegalAccessExceptionMockClass() {
    }

    public void transform(final HealthData<?> data, final Map<String, String> args) {
        // empty for tests
    }
}
