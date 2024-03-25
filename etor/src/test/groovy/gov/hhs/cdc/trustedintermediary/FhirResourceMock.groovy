package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource

class FhirResourceMock<T> implements FhirResource<T> {

    private T innerResource

    FhirResourceMock(T innerResource) {
        this.innerResource = innerResource
    }

    @Override
    public T getUnderlyingResource() {
        return innerResource
    }
}
