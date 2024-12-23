package gov.hhs.cdc.trustedintermediary.rse2e.hl7

import spock.lang.Specification

class HL7PathTest extends Specification {

    def "should create HL7Path with segment name and indices"() {
        when:
        def path = new HL7Path("MSH", [1, 2, 3] as int[])

        then:
        path.segmentName() == "MSH"
        path.indices() == [1, 2, 3] as int[]
    }

    def "equals should compare array contents"() {
        given:
        def path1 = new HL7Path("MSH", [1, 2] as int[])
        def path2 = new HL7Path("MSH", [1, 2] as int[])
        def path3 = new HL7Path("MSH", [1, 3] as int[])

        expect:
        path1 == path2
        path1 != path3
        path1 != null
        path1 == path1
    }

    def "equals should handle different segment names"() {
        given:
        def path1 = new HL7Path(segment1, [1, 2] as int[])
        def path2 = new HL7Path(segment2, [1, 2] as int[])

        expect:
        (path1 == path2) == expectedResult

        where:
        scenario          | segment1 | segment2 | expectedResult
        "same segment"    | "MSH"    | "MSH"    | true
        "both null"       | null     | null     | true
        "diff segment"    | "MSH"    | "PID"    | false
        "null vs string"  | null     | "MSH"    | false
    }

    def "hashCode should consider array contents"() {
        given:
        def path1 = new HL7Path("MSH", [1, 2] as int[])
        def path2 = new HL7Path("MSH", [1, 2] as int[])

        expect:
        path1.hashCode() == path2.hashCode()
    }

    def "toString should include array contents"() {
        given:
        def path = new HL7Path("MSH", [1, 2] as int[])

        expect:
        path.toString() == "HL7Path[segmentName=MSH, indices=[1, 2]]"
    }
}
