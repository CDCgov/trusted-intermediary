package gov.hhs.cdc.trustedintermediary.e2e

import java.nio.file.Files
import java.nio.file.Path
import spock.lang.Specification

class AuthTest extends Specification {

    def existingClientId = "report-stream"
    def validToken = Files.readString(Path.of("..", "mock_credentials", "report-stream-valid-token.jwt")).trim()

    def "a 200 valid response is returned when known organization and valid token"() {
        when:
        def response = AuthClient.authenticate(existingClientId, validToken)

        then:
        response.getCode() == 200
        def responseBody = JsonParsing.parseContent(response)
        responseBody.scope == "report-stream"
        responseBody.token_type == "bearer"
        responseBody.access_token != null
    }

    def "a 400 response is returned when request has invalid format"() {
        given:
        def invalidRequest = "%g"

        when:
        def response = AuthClient.authenticate(invalidRequest, "asdf")
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "a 401 response is returned when invalid token"() {
        given:
        def invalidToken = "invalid-token"

        when:
        def response = AuthClient.authenticate(existingClientId, invalidToken)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "a 401 response is returned when unknown organization"() {
        given:
        def invalidClientId = "invalid-client"

        when:
        def response = AuthClient.authenticate(invalidClientId, validToken)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
