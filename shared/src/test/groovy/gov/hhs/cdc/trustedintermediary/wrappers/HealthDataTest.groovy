package gov.hhs.cdc.trustedintermediary.wrappers

import spock.lang.Specification

class HealthDataTest extends Specification {
    def "default getName returns empty string"() {
        setup:
        def healthData = new IntegerHealthData(1)

        when:
        def actual = healthData.getName()

        then:
        actual == ""
    }
}

// Simple implementation of HealthData for testing
class IntegerHealthData implements HealthData<Integer> {
    private final Integer data

    IntegerHealthData(Integer data) {
        this.data = data
    }

    @Override
    Integer getUnderlyingData() {
        return data
    }
}
