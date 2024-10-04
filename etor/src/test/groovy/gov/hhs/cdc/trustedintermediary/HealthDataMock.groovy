package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.wrappers.HealthData

class HealthDataMock<T> implements HealthData<T> {

    private T innerResource

    HealthDataMock(T innerResource) {
        this.innerResource = innerResource
    }

    @Override
    T getUnderlyingData() {
        return innerResource
    }
}
