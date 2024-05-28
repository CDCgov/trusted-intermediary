package gov.hhs.cdc.trustedintermediary.external

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider
import spock.lang.Specification

class PasswordChangingHikariDataSourceTest extends Specification {
    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
    }

    def "getting the password calls the credential provider for the latest password"() {
        given:
        def mockCredentialProvider = Mock(DatabaseCredentialsProvider)
        TestApplicationContext.register(DatabaseCredentialsProvider, mockCredentialProvider)

        when:
        new PasswordChangingHikariDataSource().getPassword()

        then:
        1 * mockCredentialProvider.getPassword()
    }

    def "setting the password isn't supported"() {

        when:
        new PasswordChangingHikariDataSource().setPassword("something")

        then:
        thrown(UnsupportedOperationException)
    }
}
