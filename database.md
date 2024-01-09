# Database

## Requirements
Choose a Postgres client, [pgAdmin](https://www.pgadmin.org/) is the most full featured, intelliJ also has a built in plugin.

### Connecting to Local Database
1. Run the docker file ./docker-compose.postgres.yml in the root of the project
2. Run the `generate_env.sh` script:

    ```bash
    ./generate_env.sh
    ```

3. Run the `./gradlew clean app:run` command in your terminal
4. Configure your preferred db client to connect to the database with the connection information.  NOTE: The database runs on the non-default port of `5433` to avoid collision with the ReportStream Postgres database.
5. Grab the create table command from [line 77 of this project file](.github/workflows/terraform-deploy_reusable.yml) from after the -c to the first semi-colon, no need for the `ALTER TABLE` statements and run it in your local postgres instance to create the table
6. Upon creating the table for the first time you may have to instruct your client to refresh (In pgAdmin right-click on the left hand menu and select Refresh)

### Connecting to an Azure Hosted Database
1. Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli)
2. Open the azure environment you are trying to access from within your browser and navigate to the database
3. Inside of the Azure database page select the Networking option from the left hand nav
4. Click the link that says `Add current client IP address` and then save the page
    1. NOTE: You should only add your local IP address on a temporary basis, you should remove it after the verification is complete
5. On the left hand navigation select Authentication and select the `Add Microsoft Entra Admins` link to add your user to the list
    1. NOTE: This permission should only be added temporarily and removed after you are finished with verification
6. Enter new connection settings from Azure into your db client of choice
   1. Password will come from step 8 of these instructions and can be left blank
7. Run `az login` inside of your local terminal
8. Run `az account get-access-token --resource https://ossrdbms-aad.database.windows.net` to get a temporary password

### Modifying the database schema
To modify the schema there are a few locations in the code we need to update.
1. [Line 77 of this file contains the table creation statements](.github/workflows/terraform-deploy_reusable.yml) To modify column data types or add or remove columns, go there.
2. The `PostgresDao.java` contains our queries for the database. Remember to update both the save and the fetch methods
3. `DatabasePartnerMetadataStorage` is what calls our dao to perform the db operation, you will need to update the inputs here
