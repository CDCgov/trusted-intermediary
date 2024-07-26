package gov.hhs.cdc.trustedintermediary.context

import spock.lang.Specification

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Paths

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

        and:
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

        when:
        Files.deleteIfExists(filePath)
        Files.createFile(filePath)
        ApplicationContext.createTempFile(fileName)

        then:
        Files.exists(filePath)
    }

    def "temp directory is created when one does not already exist"() {
        given:
        def directoryName = "ti_unit_test_directory_not_already_exist"
        def directoryPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(directoryName)

        when:
        Files.deleteIfExists(directoryPath)
        ApplicationContext.createTempDirectory(directoryName)

        then:
        Files.isDirectory(directoryPath)
    }

    def "temp directory creation does not fail if directory already exists"() {
        given:
        def directoryName = "ti_unit_test_existing_directory"
        def directoryPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve(directoryName)

        when:
        Files.deleteIfExists(directoryPath)
        Files.createDirectory(directoryPath)
        ApplicationContext.createTempDirectory(directoryName)

        then:
        Files.isDirectory(directoryPath)
    }

    class InjectionDeclaringClass {
        @Inject
        private String aField

        def getAField() {
            return aField
        }
    }
}
