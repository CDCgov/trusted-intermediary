package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class DemographicsTest extends Specification {

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expected = """{"id":"an ID","destination":"Massachusetts","createdAt":"2022-12-21T08:34:27Z","client":"MassGeneral","content":null}"""

        when:
        def responseBody = Client.post("/v1/etor/demographics","""{
                "id": "an ID",
                "destination": "Massachusetts",
                "createdAt": "2022-12-21T08:34:27Z",
                "client": "MassGeneral"
            }""")

        then:
        responseBody == expected
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics","""{
                "id": "an ID",
                "destination": "Massachusetts",
                "createdAt": "2022-12-21T08:34:27Z",
                "client": "MassGeneral"
            """)
        //notice the missing end } above

        then:
        responseBody == "Server Error"
    }
}
