package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager
import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException

class PostgresDaoTest extends Specification {

    def setup(){
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }

    def "connect happy path works"(){
        given:
        def mockDriver = Mock(SqlDriverManager)
        mockDriver.getConnection(_ as String, _ as Properties) >> {Mock(Connection)}

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        PostgresDao.getInstance().driverManager.getConnection(_ as String, _ as Properties) >> {Mock(Connection)}
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def conn = PostgresDao.getInstance().getConnection()

        then:
        conn != null
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

    def "getConnection unhappy path throws exception"() {
        given:
        def mockDriver = Mock(SqlDriverManager)
        mockDriver.getConnection(_ as String, _ as Properties) >> {throw new SQLException()}
        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().getConnection()

        then:
        thrown(SQLException)
    }

    def "closeConnection unhappy path throws exception"() {
        given:
        def dao = PostgresDao.getInstance()
        def mockDriver = Mock(SqlDriverManager)
        def mockConnection = Mock(Connection)
        mockConnection.close() >> {throw new SQLException()}
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConnection
        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        dao.getConnection()
        dao.closeConnection()

        then:
        thrown(SQLException)
    }

    def "closeConnection happy path works "() {
        given:
        def dao = PostgresDao.getInstance()
        def mockDriver = Mock(SqlDriverManager)
        def mockConnection = Mock(Connection)
        mockDriver.getConnection(_ as String, _ as Properties) >> mockConnection
        TestApplicationContext.register(SqlDriverManager, mockDriver)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        dao.getConnection()
        dao.closeConnection()

        then:
        noExceptionThrown()
    }
}
