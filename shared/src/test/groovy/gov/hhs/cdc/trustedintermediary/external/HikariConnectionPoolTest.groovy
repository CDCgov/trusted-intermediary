package gov.hhs.cdc.trustedintermediary.external

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool
import spock.lang.Specification

class HikariConnectionPoolTest extends Specification {
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ConnectionPool, HikariConnectionPool.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "connection pool works" () {
        when:
        HikariConnectionPool.getInstance().getConnection()

        then:
        HikariConnectionPool.getInstance().ds.getDataSourceProperties().get("databaseName") != null
    }
}
