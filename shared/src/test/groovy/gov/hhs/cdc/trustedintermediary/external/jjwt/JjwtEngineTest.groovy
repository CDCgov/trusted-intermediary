package gov.hhs.cdc.trustedintermediary.external.jjwt

import gov.hhs.cdc.trustedintermediary.wrappers.TokenGenerationException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import spock.lang.Specification

class JjwtEngineTest extends Specification {

    def "readPrivateKey works"() {
        given:
        def expected = """SunRsaSign RSA private CRT key, 2048 bits
  params: null
  modulus: 27897662671237412614645585949757597903240726523917871693547600527580531037615927714357198420605835518634671965372032548422942203717229673323463225343709924634208255237053619724692723573411363211484198357569100148771742283580632631306619990273312871157974345578004704347658040442733544316016870094273123316267308144041089779925186312275870592286737178499633095847230908792065102806909557602796883595871117990184080094342104723065331441619179002838364572851853143344620773544234941860382864764351610571703794571553025384855153288701039759404323155524596814265271809151127402777298274741051512777127381287892775603620877
  private exponent: 6135084963443691491959954033612789413315485466621394415112910151574750195761776678565895940262471648264678116192752477289861521141867535388779067324812232613493545304240891317608890832686745995170911223102440696006419818760484425571916026211363378787010471270320472426425095867691490416933841039927237893032909032803580731575157486299449884306264507680961825930444072391195248779956131000125529322499011873765170247835498332429269555457443449344492245708187941705566103485536344264029763219575769322352195792242439276975260648301693718104261371053389186933547575290557842271013884989713398541057956598536593873481023"""

        def key = Files.readString(Path.of("..", "mock_credentials", "trusted-intermediary-private-key-local.pem"))

        when:
        def actual = JjwtEngine.getInstance().readPrivateKey(key)

        then:
        actual.properties['modulus'] == new BigInteger("27897662671237412614645585949757597903240726523917871693547600527580531037615927714357198420605835518634671965372032548422942203717229673323463225343709924634208255237053619724692723573411363211484198357569100148771742283580632631306619990273312871157974345578004704347658040442733544316016870094273123316267308144041089779925186312275870592286737178499633095847230908792065102806909557602796883595871117990184080094342104723065331441619179002838364572851853143344620773544234941860382864764351610571703794571553025384855153288701039759404323155524596814265271809151127402777298274741051512777127381287892775603620877")
        actual.properties['privateExponent'] == new BigInteger("6135084963443691491959954033612789413315485466621394415112910151574750195761776678565895940262471648264678116192752477289861521141867535388779067324812232613493545304240891317608890832686745995170911223102440696006419818760484425571916026211363378787010471270320472426425095867691490416933841039927237893032909032803580731575157486299449884306264507680961825930444072391195248779956131000125529322499011873765170247835498332429269555457443449344492245708187941705566103485536344264029763219575769322352195792242439276975260648301693718104261371053389186933547575290557842271013884989713398541057956598536593873481023")
        //        actual.toString() == expected
    }

    def "generateToken blows up because the key isn't Base64"() {
        given:
        def key = "DogCow is not actually an Base64 encoded"

        when:
        JjwtEngine.getInstance().generateToken("keyId", "issuer", "subject", "audience", 300, key)

        then:
        thrown(TokenGenerationException)
    }

    def "generateToken blows up because the key isn't valid RSA"() {
        given:
        def key = Base64.getEncoder().encodeToString("DogCow is not actually an RSA key".getBytes(StandardCharsets.UTF_8))

        when:
        JjwtEngine.getInstance().generateToken("keyId", "issuer", "subject", "audience", 300, key)

        then:
        thrown(TokenGenerationException)
    }

    def "generateToken blows up because Jjwt doesn't like something"() {
        given:
        def key = Files.readString(Path.of("..", "mock_credentials", "weak-rsa-key.pem"))

        when:
        JjwtEngine.getInstance().generateToken("keyId", "issuer", "subject", "audience", 300, key)

        then:
        thrown(TokenGenerationException)
    }

    def "getExpirationDate works with unexpired JWT"() {
        given:
        def pemKey = Files.readString(Path.of("..", "mock_credentials", "trusted-intermediary-private-key-local.pem"))
        def secondsFromNowExpiration = 300
        def expectedExpirationCenter = LocalDateTime.now().plusSeconds(secondsFromNowExpiration).truncatedTo(ChronoUnit.SECONDS)
        def expectedExpirationUpper = expectedExpirationCenter.plusSeconds(3)  // 3 seconds is the window in which we expect the the code below to execute to generate the JWT (which is very generous, it should take much shorter than this)

        def jwt = JjwtEngine.getInstance().generateToken("DogCow", "Dogcow", "subject", "fake_URL", secondsFromNowExpiration, pemKey)

        when:
        def actualExpiration = JjwtEngine.getInstance().getExpirationDate(jwt)

        then:
        //testing that the actual expiration is between (inclusive) the expectedExpirationCenter and expectedExpirationUpper time
        (actualExpiration.isEqual(expectedExpirationCenter) || actualExpiration.isAfter(expectedExpirationCenter)) && (actualExpiration.isEqual(expectedExpirationUpper) || actualExpiration.isBefore(expectedExpirationUpper))
    }

    def "getExpirationDate works when the JWT is expired"() {
        given:
        def fileName = "report-stream-expired-token"
        def expiredToken = Files.readString(Path.of("..", "mock_credentials", fileName + ".jwt"))

        when:
        def actual = JjwtEngine.getInstance().getExpirationDate(expiredToken)
        def expected = LocalDateTime.ofInstant(Instant.ofEpochSecond(1683209064L), ZoneId.systemDefault())

        then:
        actual == expected
    }
}
