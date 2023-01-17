package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class OrderTest extends Specification {

    def "order message is returned from demo ETOR order endpoint"() {
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

    def "bad response given poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/order","""{
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
