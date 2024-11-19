package gov.hhs.cdc.trustedintermediary.etor.utils.security

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.Order
import gov.hhs.cdc.trustedintermediary.etor.orders.SendOrderUseCase
import gov.hhs.cdc.trustedintermediary.etor.results.Result
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class HashHelperTest extends Specification {
    def mockLogger = Mock(Logger)
    def hashHelper = new HashHelper()

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(Logger, mockLogger)
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "generateHash generates hash for an order"() {
        given:
        def mockOrder = Mock(Order)

        when:
        String mockHash = hashHelper.generateHash(mockOrder)

        then:
        mockHash !== ""
        0 * mockLogger.logError(_, _)
    }

    def "generateHash generates the same hash for the same object"() {
        given:
        def mockResult = Mock(Result)
        def mockResult2 = mockResult

        when:
        String mockHash = hashHelper.generateHash(mockResult)
        String mockHash2 = hashHelper.generateHash(mockResult2)

        then:
        mockHash !== ""
        mockHash == mockHash2
        0 * mockLogger.logError(_, _)
    }
}
