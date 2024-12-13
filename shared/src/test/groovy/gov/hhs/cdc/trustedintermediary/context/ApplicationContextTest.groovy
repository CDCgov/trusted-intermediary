package gov.hhs.cdc.trustedintermediary.context

import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Paths

class ApplicationContextTest extends Specification {

    interface TestingInterface {
        void test()
    }

    class NonSingletonClazz {
        @Inject
        Logger logger
        void test() {}
    }

    static class DogCow implements TestingInterface {

        @Override
        void test() {
            print("test()")
        }
    }

    static class DogCowTwo implements TestingInterface {

        @Override
        void test() {
            print("testTwo()")
        }
    }
    def DOGCOW = new DogCow()
    def DOGCOWTWO = new DogCowTwo()

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

    def "implementors retrieval test"() {
        setup:
        def dogCow = DOGCOW
        def dogCowTwo = DOGCOWTWO
        def implementors = new HashSet()
        implementors.add(DogCow)
        implementors.add(DogCowTwo)

        expect:
        implementors == ApplicationContext.getImplementors(TestingInterface)
    }

    def "injectIntoNonSingleton unhappy path"() {
        given:
        def nonSingletonClass = new NonSingletonClazz()
        def object = new Object()
        ApplicationContext.register(Logger, object)
        when:
        ApplicationContext.injectIntoNonSingleton(nonSingletonClass)
        then:
        thrown(IllegalArgumentException)
    }

    def "injectIntoNonSingleton unhappy path when fieldImplementation runs into an error"() {
        given:
        def nonSingletonClass = new NonSingletonClazz()
        when:
        ApplicationContext.injectIntoNonSingleton(nonSingletonClass)
        then:
        thrown(IllegalArgumentException)
    }

    def "injectIntoNonSingleton unhappy path when fieldImplementation is null"() {
        given:
        def nonSingletonClass = new NonSingletonClazz()
        when:
        ApplicationContext.skipMissingImplementations = true
        ApplicationContext.injectIntoNonSingleton(nonSingletonClass)
        then:
        noExceptionThrown()
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

    def "isPropertyNullOrBlank returns true when property is null or empty"() {
        when:
        def isPresentWhenNull = ApplicationContext.isPropertyPresent("nonExistentProperty")

        then:
        !isPresentWhenNull

        when:
        TestApplicationContext.addEnvironmentVariable("emptyProperty", "")
        def isPresentWhenEmpty = ApplicationContext.isPropertyPresent("emptyProperty")

        then:
        !isPresentWhenEmpty
    }

    def "temp file is created when one does not already exist"() {
        given:
        def fileName = "ti_unit_test_file_not_already_exist.txt"
        def filePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(fileName)

        and: "the temp file does not exist"
        Files.deleteIfExists(filePath)

        when:
        ApplicationContext.createTempFile(fileName)

        then:
        Files.exists(filePath)

        cleanup:
        Files.deleteIfExists(filePath)
    }

    def "temp file creation does not fail if file already exists"() {
        given:
        def fileName = "ti_unit_test_existing_file.txt"
        def filePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(fileName)

        and: "temp file already exists"
        Files.deleteIfExists(filePath)
        Files.createFile(filePath)

        when:
        ApplicationContext.createTempFile(fileName)

        then:
        Files.exists(filePath)

        cleanup: "remove temp file"
        Files.deleteIfExists(filePath)
    }

    def "temp directory is created when one does not already exist"() {
        given:
        def directoryName = "ti_unit_test_directory_not_already_exist"
        def directoryPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(directoryName)

        and:
        Files.deleteIfExists(directoryPath)

        when:
        ApplicationContext.createTempDirectory(directoryName)

        then:
        Files.isDirectory(directoryPath)

        cleanup:
        Files.deleteIfExists(directoryPath)
    }

    def "temp directory creation does not fail if directory already exists"() {
        given:
        def directoryName = "ti_unit_test_existing_directory"
        def directoryPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(directoryName)

        and:
        Files.deleteIfExists(directoryPath)
        Files.createDirectory(directoryPath)

        when:
        ApplicationContext.createTempDirectory(directoryName)

        then:
        Files.isDirectory(directoryPath)

        cleanup:
        Files.deleteIfExists(directoryPath)
    }

    def "registering an unsupported injection class"() {
        given:
        def injectedValue = "DogCow"
        def injectionInstantiation = new InjectionDeclaringClass()

        TestApplicationContext.register(List.class, injectionInstantiation)
        // notice above that I'm registering the injectionInstantiation object as a List class.
        // injectionInstantiation is of class InjectionDeclaringClass,
        // and InjectionDeclaringClass doesn't implement List (it only implements AFieldInterface).
        TestApplicationContext.register(String.class, injectedValue)

        when:
        TestApplicationContext.injectRegisteredImplementations()
        injectionInstantiation.getAField()

        then:
        noExceptionThrown()
    }

    class InjectionDeclaringClass {
        @Inject
        private String aField

        def getAField() {
            return aField
        }
    }
}
