package gov.hhs.cdc.trustedintermediary.context

import spock.lang.Specification

import javax.inject.Inject

class ApplicationContextTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
    }

    def "implementation retrieval test"() {
        setup:
        def result = "DogCow"
        ApplicationContext.register(String.class, "DogCow")

        expect:
        result == ApplicationContext.getImplementation(String.class)
    }

    def "implementation injection test"() {
        given:
        def injectedValue = "DogCow"
        def injectionInstantiation = new InjectionDeclaringClass()
        TestApplicationContext.register(String, injectedValue)
        TestApplicationContext.register(InjectionDeclaringClass, injectionInstantiation)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def aFieldValue = injectionInstantiation.getAField()

        then:
        injectedValue == aFieldValue
    }

    def "returns an environmental status"() {

        when:
        def environmentStatus = ApplicationContext.getEnvironment()

        then:
        environmentStatus == "local"
    }

    class InjectionDeclaringClass {
        @Inject
        private String aField

        def getAField() {
            return aField
        }
    }
}
