package gov.hhs.cdc.trustedintermediary.external.hikari

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import spock.lang.Specification

class HikariConnectionPoolTest extends Specification {
    def defaultLifetime = 1800000L

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.addEnvironmentVariable("DB_USER", "test_user")
        TestApplicationContext.addEnvironmentVariable("DB_URL", "test_url")
        TestApplicationContext.addEnvironmentVariable("DB_NAME", "test_name")
        TestApplicationContext.addEnvironmentVariable("DB_PORT", "1234")
    }

    def "connection pool works" () {
        when:
        TestApplicationContext.addEnvironmentVariable("DB_MAX_LIFETIME", "9001")
        def result = HikariConnectionPool.constructHikariDataSource()

        then:
        result.getUsername() == "test_user"
        result.getDataSourceProperties().get("serverName") == "test_url"
        result.getDataSourceProperties().get("databaseName") == "test_name"
        result.getDataSourceProperties().get("portNumber") == "1234"
        result.getMaxLifetime() == 9001L
    }

    def "connection pool works with default DB_MAX_LIFETIME" () {
        when:
        TestApplicationContext.addEnvironmentVariable("DB_MAX_LIFETIME", "")
        def result = HikariConnectionPool.constructHikariDataSource()

        then:
        result.getMaxLifetime() == defaultLifetime
    }

    def "connection pool uses default DB_MAX_LIFETIME when a NumberFormatException is thrown" () {
        when:
        TestApplicationContext.addEnvironmentVariable("DB_MAX_LIFETIME", "kjihugyftrd")
        def result = HikariConnectionPool.constructHikariDataSource()

        then:
        result.getMaxLifetime() == defaultLifetime
    }

    def "connection pool uses default DB_MAX_LIFETIME if an override value is not set" () {
        when:
        def result = HikariConnectionPool.constructHikariDataSource()

        then:
        result.getMaxLifetime() == defaultLifetime
    }
}
