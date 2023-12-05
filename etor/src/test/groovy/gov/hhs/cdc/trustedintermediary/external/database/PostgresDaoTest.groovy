package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.SqlDriverManager
import spock.lang.Specification

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.sql.SQLException

class PostgresDaoTest extends Specification {

    def setup(){
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }

    def "gets connection"(){

        given:

        def mockDriver = Mock(SqlDriverManager)
        mockDriver.getConnection(_, _) >> {Mock(Connection)}

        TestApplicationContext.register(SqlDriverManager, mockDriver)
        PostgresDao.getInstance().driverManager.getConnection(_ as String, _ as Properties) >> {Mock(Connection)}
        TestApplicationContext.injectRegisteredImplementations()
        when:

        def conn = PostgresDao.getInstance().getConnection()

        then:

        conn != null
    }

        def "getting a connection throws an exception"() {
            given:


            def mockDriver = Mock(SqlDriverManager)
            mockDriver.getConnection(_ as String, _ as Properties) >> {throw new SQLException()}

            TestApplicationContext.register(SqlDriverManager, mockDriver)
            TestApplicationContext.injectRegisteredImplementations()


            when:
            PostgresDao.getInstance().connect()

            then:
            thrown(Exception)

        }
}
