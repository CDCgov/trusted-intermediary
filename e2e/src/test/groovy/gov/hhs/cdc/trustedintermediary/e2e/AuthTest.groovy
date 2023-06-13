package gov.hhs.cdc.trustedintermediary.e2e

import java.nio.file.Files
import java.nio.file.Path
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import spock.lang.Specification

class AuthTest extends Specification {

    def authEndpointPath = "/v1/auth"
    def postBody = { scope, client_assertion -> "scope=${scope}&client_assertion=${client_assertion}" }
    def existingClientId = "report-stream"
    def validToken = new String(
    Files.readAllBytes(
    Path.of("..", "mock_credentials", "report-stream-valid-token.jwt")
    ))

    def "a 200 valid response is returned when known organization and valid token"() {
        when:
        def response = AuthClient.loginRaw(existingClientId, validToken)

        then:
        response.getCode() == 200
        def body = EntityUtils.toString(response.getEntity())
        def responseBody = JsonParsing.parse(body, Map.class)
        responseBody.scope == "report-stream"
        responseBody.token_type == "bearer"
        responseBody.access_token != null
    }

    def "a 400 response is returned when request has invalid format"() {
        given:
        def invalidRequest = "%g"

        when:
        def response = AuthClient.loginRaw(invalidRequest, "asdf")

        then:
        response.getCode() == 400
    }

    def "a 401 response is returned when poorly formatted request"() {
        given:
        def invalidRequest = "invalid-request"

        when:
        def response = Client.post(authEndpointPath, invalidRequest, ContentType.APPLICATION_FORM_URLENCODED, Map.of())

        then:
        response.getCode() == 401
    }

    def "a 401 response is returned when invalid token"() {
        given:
        def invalidToken = "invalid-token"

        when:
        def response = AuthClient.loginRaw(existingClientId, invalidToken)

        then:
        response.getCode() == 401
    }

    def "a 401 response is returned when unknown organization"() {
        given:
        def invalidClientId = "invalid-client"

        when:
        def response = AuthClient.loginRaw(invalidClientId, validToken)

        then:
        response.getCode() == 401
    }
}
