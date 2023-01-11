package gov.hhs.cdc.trustedintermediary

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.YamlCombiner
import spock.lang.Specification

class OpenApiTest extends Specification {
    void setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def "baseline documentation has an OpenAPI spec"() {
        when:
        def baselineOpenApiSpec = OpenApi.getInstance().getBaselineDocumentation()

        then:
        noExceptionThrown()
        !baselineOpenApiSpec.isEmpty()
        baselineOpenApiSpec.contains("paths:")
    }

    def "YAML combiner is called"() {
        given:
        def mockYamlCombinerResponse = "DogCow"

        def yamlCombinerMock = Mock(YamlCombiner)
        yamlCombinerMock.combineYaml(_ as Set) >> mockYamlCombinerResponse
        TestApplicationContext.register(YamlCombiner, yamlCombinerMock)

        def setOfOtherOpenApiSpecs = ["Moof", "Clarus"] as Set

        when:
        def openApiSpec = OpenApi.getInstance().generateApiDocumentation(setOfOtherOpenApiSpecs)

        then:
        noExceptionThrown()
        openApiSpec == mockYamlCombinerResponse
        1 * yamlCombinerMock.combineYaml(_ as Set) >> { Set<String> setArgument ->
            assert setArgument.containsAll(setOfOtherOpenApiSpecs)
        }
    }
}
