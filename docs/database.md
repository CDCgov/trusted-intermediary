# Database

## Requirements
Choose a Postgres client, [pgAdmin](https://www.pgadmin.org/) is the most full featured, IntelliJ also has a built-in plugin.

## Connecting to Local Database
1. Run the docker file ./docker-compose.postgres.yml in the root of the project
2. Run the `generate_env.sh` script:

    ```shell
    ./generate_env.sh
    ```

3. Run the `./gradlew clean app:run` command in your terminal
4. Configure your preferred db client to connect to the database with the connection information.  NOTE: The database runs on the non-default port of `5433` to avoid collision with the ReportStream Postgres database.
5. Grab the create table command from [line 77 of this project file](.github/workflows/terraform-deploy_reusable.yml) from after the -c to the first semi-colon, no need for the `ALTER TABLE` statements and run it in your local postgres instance to create the table
6. Upon creating the table for the first time you may have to instruct your client to refresh (In pgAdmin right-click on the left hand menu and select Refresh)

## Connecting to an Azure Hosted Database
1. For a CDC domain-hosted environment, connect to the VPN.  See the
   [VPN documentation](https://www.notion.so/flexion-cdc-ti/DRAFT-Azure-VPN-pieces-d814ddcb87b1467f93ccf473e9cdb69c?pvs=4).
2. Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli)
3. Open the Azure environment you are trying to access from within your browser and navigate to the database
4. Inside of the Azure database page select the Networking option from the left hand nav
5. For a Flexion domain-hosted environment, click the link that says `Add current client IP address` and then save the page
    1. NOTE: You should only add your local IP address on a temporary basis, you should remove it after the verification is complete
6. On the left hand navigation select Authentication and select the `Add Microsoft Entra Admins` link to add your user to the list. Select Okay and then save the underlying page
    1. NOTE: This permission should only be added temporarily and removed after you are finished with verification
7. Enter new connection settings from Azure into your db client of choice
   1. Password will come from step 8 of these instructions and can be left blank
8. Run `az login` inside of your local terminal
9. Run `az account get-access-token --resource https://ossrdbms-aad.database.windows.net` to get a temporary password

## Modifying the database schema
To modify the schema there are a few locations in the code we need to update.
1. Add the update inside the databaseMigrations folder in the repo.  See the [section on migrations](#database-migrations) for details
2. The `PostgresDao.java` contains our queries for the database. Remember to update both the save and the fetch methods
3. `DatabasePartnerMetadataStorage` is what calls our dao to perform the db operation, you will need to update the inputs here

## Database Migrations

We use [Liquibase](https://www.liquibase.com/download) to handle the migrations.  Our migration files are located in
[`/etor/databaseMigrations`](/etor/databaseMigrations).

### Adding Migrations

Documentation on Liquibase can be found [here](https://docs.liquibase.com/).

Our change log files are organized by business domain.  For example, `metadata.yml` includes migrations about our
metadata.  You may need to create a new file if the migrations are for a business domain not previously covered.  Add
the new file to [`/etor/databaseMigrations`](/etor/databaseMigrations) with the name of the business domain.  For
example, `dogcow.yml` for migrations associated with DogCows.  Liquibase supports multiple change log file types, but we
prefer YAML.  YAML supports specialized change types but also supports arbitrary SQL if the flexibility is needed.

A couple of concepts we adhere to...
1. For each new file, start the `id` at 1 and increment from there.
2. Use your GitHub username as the `author`.
3. Provide a `comment` that better describes what or why you are trying to accomplish.
4. If a migration only applies to Azure, include a `label` of `azure`.
5. Include only one change per `changeSet`.

Reference this new file in [`root.yml`](/etor/databaseMigrations/root.yml) by adding it at the bottom of all the other
includes except for any includes that are required to be last (e.g. the `etor/databaseMigrations/azure.yml` include).

### Running

Our deployed databases have migrations automatically ran by our CD processes.

To run migrations when running our database locally, run the following...

```shell
liquibase update --changelog-file ./etor/databaseMigrations/root.yml --url jdbc:postgresql://localhost:5433/intermediary --username intermediary --password 'changeIT!' --label-filter '!azure'
```

Notice the `--label-filter '!azure'`.  This will prevent the Azure-specific migrations from running and failing in the
local environment.

### Reverting changes
To revert changes in the Azure database we use The Github Actions runner.
1. Find the runner named Rollback DB in the repository under the `Actions` tab
2. Select Run workflow
3. Run the workflow (Example inputs below)
   - Use workflow from: Always Main
   - Number of migrations to rollback, an integer that is specific to the file input
   - File with migrations you want to roll back: This should be the file containing the specific sub-file in the `databaseMigrations` directory you want to rollback from EX: `./etor/databaseMigrations/metadata.yml`
   - Azure environment to rollback on: Select the environment you want to see the changes reverted from
4. Once you have the inputs entered select Run Workflow on the tab and the workflow should start up

To rollback changes locally use the `rollback-count` command and pass in an argument for the number of rollbacks using `--count=?`
