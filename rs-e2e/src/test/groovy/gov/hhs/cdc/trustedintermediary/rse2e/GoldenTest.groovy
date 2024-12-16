package gov.hhs.cdc.trustedintermediary.rse2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class GoldenTest extends Specification {

    def beforeFileJsonFileString = Files.readString(Path.of("../examples/Test/Automated/golden_before.fhir"))
    def afterFileJsonFileString = Files.readString(Path.of("../examples/Test/Automated/golden_after.fhir"))

    def setup() {
    }
    // hola mi amigo fdfdfdji. senor james. el
    def "Golden Test"() {
        // notes:
        // Use workflow to sendoff the file (happens via Github Action)


        // compare with known good file
        // def matchedFiles = fileMatcher.matchFiles(azureFiles, localFiles)
    }
}
