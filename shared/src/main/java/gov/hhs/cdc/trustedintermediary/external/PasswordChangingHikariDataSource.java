package gov.hhs.cdc.trustedintermediary.external;

import com.zaxxer.hikari.HikariDataSource;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider;

/**
 * This class extends HikariDataSource and overrides the getPassword method to retrieve the most
 * up-to-date password from the {@link DatabaseCredentialsProvider}. This is because passwords can
 * change over time.
 */
public class PasswordChangingHikariDataSource extends HikariDataSource {

    @Override
    public String getPassword() {
        return ApplicationContext.getImplementation(DatabaseCredentialsProvider.class)
                .getPassword();
    }

    @Override
    public void setPassword(String newPassword) {
        throw new UnsupportedOperationException(
                "Password changing using this method is not supported");
    }
}
