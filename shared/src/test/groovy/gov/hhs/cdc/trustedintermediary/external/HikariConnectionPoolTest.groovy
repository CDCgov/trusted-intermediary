package gov.hhs.cdc.trustedintermediary.external

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider
import spock.lang.Specification

class HikariConnectionPoolTest extends Specification {
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        def credProviders = Mock(DatabaseCredentialsProvider)
        TestApplicationContext.addEnvironmentVariable("DB_USER", "test_user")
        TestApplicationContext.addEnvironmentVariable("DB_URL", "test_url")
        TestApplicationContext.addEnvironmentVariable("DB_NAME", "test_name")
        TestApplicationContext.addEnvironmentVariable("DB_PORT", "1234")
        TestApplicationContext.addEnvironmentVariable("DB_PASS", "test_pass")

        credProviders.getPassword() >> "test_pass"
        TestApplicationContext.register(DatabaseCredentialsProvider, credProviders)
        TestApplicationContext.injectRegisteredImplementations()
    }

    def "connection pool works" () {
        when:
        def result = HikariConnectionPool.constructHikariConfig()

        then:
        result.getDataSourceProperties().get("user") == "test_user"
    }
}
