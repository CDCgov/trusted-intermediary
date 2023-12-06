package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager
import spock.lang.Specification

import java.time.Instant

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class PostgresDaoTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }

    def "connect happy path works"(){
        given:
        def mockDriver = Mock(SqlDriverManager)
        Connection mockConn
        mockDriver.getConnection(_ as String, _ as Properties) >> {mockConn}

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def conn = PostgresDao.getInstance().connect()

        then:
        conn == mockConn
    }

    def "connect unhappy path throws exception"() {
        given:
        def mockDriver = Mock(SqlDriverManager)
        mockDriver.getConnection(_ as String, _ as Properties) >> {throw new SQLException()}
        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().connect()

        then:
        thrown(SQLException)
    }

    def "upsertMetadata works"() {
        given:
        def upsertMockDriver = Mock(SqlDriverManager)
        Connection upsertMockConn = Mock(Connection)
        PreparedStatement upsertMockStatement = Mock(PreparedStatement)


        upsertMockDriver.getConnection(_ as String, _ as Properties) >>  upsertMockConn
        upsertMockConn.prepareStatement(_ as String) >> upsertMockStatement

        TestApplicationContext.register(SqlDriverManager, upsertMockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertMetadata("mock_id", "mock_sender", "mock_receiver", "mock_hash", Instant.now())

        then:
        1 * upsertMockStatement.executeUpdate()
    }
}
