package gov.hhs.cdc.trustedintermediary.context


import spock.lang.Specification

class ReflectionTest extends Specification {
    def "can find an implementation"() {
        when:
        def classes = Reflection.getImplementors(Comparable)

        then:
        classes.contains(DogCow)
    }

    static class DogCow implements Comparable {
        @Override
        int compareTo(final Object o) {
            return 0
        }
    }
}
