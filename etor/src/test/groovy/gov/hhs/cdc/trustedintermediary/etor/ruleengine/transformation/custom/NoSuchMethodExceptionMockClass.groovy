package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource

class NoSuchMethodExceptionMockClass {
    private String imNotNull

    NoSuchMethodExceptionMockClass(String notNullConstructor) {
        imNotNull = notNullConstructor
    }

    public void noTransform(final FhirResource<?> resource, final Map<String, String> args) {
        //No such method
    }
}
