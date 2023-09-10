package gov.hhs.cdc.trustedintermediary.wrappers

import gov.hhs.cdc.trustedintermediary.external.formatter.TypeReference
import spock.lang.Specification

class TypeReferenceTest extends Specification {
    def "test TypeReference"() {
        setup:
        def typeReference = new TypeReference<Map<String,List<Integer>>>() {}
        def expectedTypeName = "java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"

        when:
        def actualTypeName = typeReference.getType().typeName

        then:
        actualTypeName == expectedTypeName
    }
}
