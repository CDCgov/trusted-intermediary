package gov.hhs.cdc.trustedintermediary.e2e

import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

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
        def response = Client.requestPost(authEndpointPath, postBody(existingClientId, validToken), ContentType.APPLICATION_FORM_URLENCODED)

        then:
        def httpResponse = (ClassicHttpResponse) response.returnResponse()
        httpResponse.getCode() == 200
        def body = EntityUtils.toString(httpResponse.getEntity())
        def responseBody = JsonParsing.parse(body, Map.class)
        responseBody.scope == "report-stream"
        responseBody.token_type == "bearer"
        responseBody.access_token != null
    }

    def "a 400 response is returned when request has invalid format"() {
        given:
        def invalidRequest= "%g"

        when:
        def response = Client.requestPost(authEndpointPath, invalidRequest, ContentType.APPLICATION_FORM_URLENCODED)

        then:
        response.returnResponse().getCode() == 400
    }

    def "a 401 response is returned when poorly formatted request"() {
        given:
        def invalidRequest= "invalid-request"

        when:
        def response = Client.requestPost(authEndpointPath, invalidRequest, ContentType.APPLICATION_FORM_URLENCODED)

        then:
        response.returnResponse().getCode() == 401
    }

    def "a 401 response is returned when invalid token"() {
        given:
        def invalidToken = "invalid-token"

        when:
        def response = Client.requestPost(authEndpointPath, postBody(existingClientId, invalidToken), ContentType.APPLICATION_FORM_URLENCODED)

        then:
        response.returnResponse().getCode() == 401
    }

    def "a 401 response is returned when unknown organization"() {
        given:
        def invalidClientId = "invalid-client"

        when:
        def response = Client.requestPost(authEndpointPath, postBody(invalidClientId, validToken), ContentType.APPLICATION_FORM_URLENCODED)

        then:
        response.returnResponse().getCode() == 401
    }
}
