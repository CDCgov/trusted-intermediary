package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData


class NoSuchMethodExceptionMockClass {
    private String imNotNull

    NoSuchMethodExceptionMockClass(String notNullConstructor) {
        imNotNull = notNullConstructor
    }

    public void noTransform(final HealthData<?> data, final Map<String, String> args) {
        //No such method
    }
}
