package gov.hhs.cdc.trustedintermediary.external.database

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

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

        TestApplicationContext.injectRegisteredImplementations()
        when:

        def conn = PostgresDao.getInstance().getConnection()

        then:

        conn != null

    }

//    def "getting a connection throws an exception"() {
//        given:
//        def sqlException = new SQLException()
//        def driverManager = Mock(DriverManager)
//
//        driverManager.getConnection(_ as String, _ as Properties) >> { throw new SQLException()}
//        //DriverManager.getMetaClass().
//        TestApplicationContext.register(DriverManager, driverManager)
//        TestApplicationContext.injectRegisteredImplementations()
//        when:
//        PostgresDao.getInstance().getConnection()
//        then:
//        thrown(SQLException)
//
//    }
}
