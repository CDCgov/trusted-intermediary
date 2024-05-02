package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirImplementation
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiFhirResource
import org.hl7.fhir.r4.model.Bundle

import java.nio.file.Files

class ExamplesHelper {

    static List<HapiFhirResource> getExampleFhirResources(String messageType = "") {
        def testExampleFilesPath = ApplicationContext.getExamplesPath().resolve("Test")
        return Files.walk(testExampleFilesPath.resolve(messageType))
                .filter { it.toString().endsWith(".fhir") }
                .map { new HapiFhirResource(HapiFhirImplementation.getInstance().parseResource(Files.readString(it), Bundle))  }
                .collect()
    }

    static HapiFhirResource getExampleFhirResource(String relativeFilePath) {
        def testExampleFilesPath = ApplicationContext.getExamplesPath().resolve("Test")
        def resourceFilePath = testExampleFilesPath.resolve(relativeFilePath)
        def parsedResource = HapiFhirImplementation.getInstance().parseResource(Files.readString(resourceFilePath), Bundle)
        return new HapiFhirResource(parsedResource)
    }
}
