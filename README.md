# CDC Trusted Intermediary

> [!WARNING]
> This application is no longer being developed or maintained.

This document provides instructions for setting up the environment, running the application, and performing various tasks such as compiling, testing, and contributing to the project.

## Table of Contents

- [CDC Trusted Intermediary](#cdc-trusted-intermediary)
- [Table of Contents](#table-of-contents)
  - [Requirements](#requirements)
  - [Using and Running](#using-and-running)
    - [Generating and using a token](#generating-and-using-a-token)
  - [Development](#development)
    - [Additional Requirements](#additional-requirements)
    - [Generating .env File](#generating-env-file)
    - [Using a local database](#using-a-local-database)
    - [Compiling](#compiling)
    - [Testing](#testing)
      - [Unit Tests](#unit-tests)
      - [End-to-end Tests](#end-to-end-tests)
      - [Automated ReportStream Integration/End-to-End Test](#automated-reportstream-integrationend-to-end-test)
      - [Load Testing](#load-testing)
    - [Debugging](#debugging)
      - [Attached JVM Config for IntelliJ](#attached-jvm-config-for-intellij)
      - [Docker Container Debugging Using Java Debug Wire Protocol (JDWP)](#docker-container-debugging-using-java-debug-wire-protocol-jdwp)
      - [Steps](#steps)
    - [Deploying](#deploying)
      - [Environments](#environments)
        - [Internal](#internal)
        - [Dev](#dev)
        - [Staging](#staging)
        - [Prod](#prod)
      - [Initial Azure and GitHub Configuration](#initial-azure-and-github-configuration)
      - [Interacting with Deployed Environments](#interacting-with-deployed-environments)
        - [Application](#application)
        - [Database](#database)
    - [Pre-Commit Hooks](#pre-commit-hooks)
    - [Contributing](#contributing)
    - [Database](#database-1)
    - [Setup with ReportStream](#setup-with-reportstream)
      - [CDC-TI Setup](#cdc-ti-setup)
      - [ReportStream Setup](#reportstream-setup)
      - [Submit request to ReportStream](#submit-request-to-reportstream)
        - [Locally](#locally)
          - [Orders](#orders)
          - [Results](#results)
        - [Staging](#staging-1)
  - [DORA Metrics](#dora-metrics)
  - [Related Documents](#related-documents)
  - [CDC Notices](#cdc-notices)
    - [Public Domain Standard Notice](#public-domain-standard-notice)
    - [License Standard Notice](#license-standard-notice)
    - [Privacy Standard Notice](#privacy-standard-notice)
    - [Records Management Standard Notice](#records-management-standard-notice)
    - [Additional Standard Notices](#additional-standard-notices)
    - [Troubleshooting](#troubleshooting)

## Requirements

Any distribution of the Java 17 JDK.

## Using and Running

To run the application directly, execute...

```shell
./gradlew clean run
```

This runs the web API on port 8080. The app reads/writes data to a local file (unless you have a DB configured)

You can view the API documentation at `/openapi`.

### Generating and using a token

1. Run `brew install mike-engel/jwt-cli/jwt-cli`
2. Replace `PATH_TO_FILE_ON_YOUR_MACHINE` in this command with the actual path, then run it: `jwt encode --exp='+5min' --jti $(uuidgen) --alg RS256  --no-iat -S @/PATH_TO_FILE_ON_YOUR_MACHINE/trusted-intermediary/mock_credentials/organization-trusted-intermediary-private-key-local.pem`
3. Copy token from terminal and paste into your postman body with the key `client_assertion`
4. Add a key to the body with the key `scope` and value of `trusted-intermediary`
5. Body type should be `x-wwww-form-urlencoded`
6. You should be able to run the post call against the `v1/auth/token` endpoint to receive a bearer token [to be used in this step](#submit-request-to-reportstream)

## Development

### Additional Requirements

The additional requirements needed to contribute towards development are...

- [Pre-Commit](https://pre-commit.com)
- [Locust.io](https://docs.locust.io/en/stable/installation.html)
- [Python](https://docs.python-guide.org/starting/installation/)
- [Terraform](https://www.terraform.io)
- [Liquibase](https://www.liquibase.com/download)
- [Docker](https://www.docker.com/)

### Generating .env File

To set up the necessary environment variables, run the `generate_env.sh` script. This script
creates a `.env` file in the resource folder with the required configuration

1. Navigate to the project directory.
2. Run the `generate_env.sh` script:

   ```bash
   ./generate_env.sh
   ```

3. If you run TI using Docker rather than Gradle, update the DB and port values in the `.env` file (the alternate values are in comments)

### Using a local database

Use [docker-compose.yml](docker-compose.yml) to run your local DB. In IntelliJ, you can click the play arrow to start it

![docker-postgres.png](images/docker-postgres.png)

Apply all outstanding migrations:

```bash
liquibase update --changelog-file ./etor/databaseMigrations/root.yml --url jdbc:postgresql://localhost:5433/intermediary --username intermediary --password 'changeIT!' --label-filter '!azure'
```

If running in Windows, use double quotes instead:

```shell
liquibase update --changelog-file ./etor/databaseMigrations/root.yml --url jdbc:postgresql://localhost:5433/intermediary --username intermediary --password "changeIT!" --label-filter "!azure"
```

### Compiling

To compile the application, execute...

```shell
./gradlew shadowJar
```

Once compiled, the built artifact is `/app/build/libs/app-all.jar`.

### Testing

#### Unit Tests

To run the unit tests, execute...

```shell
./gradlew clean allUnitTests
```

#### End-to-end Tests

End-to-end tests are designed to interact with the API and verify that its overall flow operates correctly. They require that the API to be running already.
The end-to-end tests use whatever database configuration is already in place - if you're using the local filesystem,
so will the e2e tests (this is how they work on github), and if you're using a DB, so will the tests

To run them, execute...

```shell
./gradlew e2e:clean e2e:test
```

The previous command requires the API to be running already. To help streamline the execution of this flow, a helper Bash script can be executed...

```shell
./e2e-execute.sh
```

This will start the API, wait for it to respond, run the end-to-end tests against that running API, and then stop the API.

These tests are located under the `e2e` Gradle sub-project directory.  Like any Gradle project, there are the `main` and `test` directories.
The `test` directory contains the tests.  The `main` directory contains our custom framework that helps us interact with the API.

#### Automated ReportStream Integration/End-to-End Test

These tests cover the integration between ReportStream and TI. They run automatically every
weekday via Github actions. See [the rs-e2e readme](rs-e2e/README.md) for more details.

#### Load Testing

Load tests are completed with [Locust.io](https://docs.locust.io/en/stable/installation.html).

##### Running Locally

Run the load tests by running...

```shell
./gradle-load-execute.sh

./docker-load-execute.sh
```

The Gradle version runs our API via Gradle.  The Docker version JARs our application and runs it in Docker.

This will run the API for you, so no need to run it manually.

>**Note:**
>
>**If you are already running the API, stop it before running the load tests or the cleanup steps won't work.**

The load tests will also spin up (and clean up) a local test DB on port 5434 that should not interfere with the local dev DB.

The `locustfile.py` that specifies the load test is located at
[`./operations/locustfile.py`](./operations/locustfile.py).

If you want to run the load test in an interactive mode, run...

```shell
locust -f ./operations/locustfile.py
```

The terminal will start a local web interface, and you can enter
the swarm parameters for the test and the local url where the app is running
(usually `http://localhost:8080`).  You can also set time limits for the tests under 'Advanced Settings'.

##### Running and Creating in Azure

To run, navigate to the
[Azure Load Tests GitHub Action](https://github.com/CDCgov/trusted-intermediary/actions/workflows/azure-load-tests.yml)
and click on Run workflow.

To create a new load test in Azure, the subscription first needs to be opted into Azure's Locust preview feature.
Sadly, the Azure Terraform provider doesn't support load tests so we need to create this via ClickOps.  Therefore, you can follow these steps...

1. Navigate to the Azure Portal with the `?Microsoft_Azure_CloudNativeTesting_locust=true` query parameter.  For
   example, this [link](https://portal.azure.com/?Microsoft_Azure_CloudNativeTesting_locust=true) will work.
2. Navigate or search for the Azure Load Testing service and click Create.
   1. Walk through the wizard, but make sure to pick the same resource group as the environment you plan to test.
3. After creation, navigate to the Identity slice, which is under the Settings group, of your new load test.
   1. Turn the Status to On under the System assigned tab and click Save.
4. Navigate to the TI key vault in the same resource group as the load test.
   1. Navigate to the Secrets slice, under the Objects group, and click Generate/Import.
   2. Provide the name `trusted-intermediary-valid-token-jwt`.
   3. The secret value should be a newly created JWT that won't expire in a long time using the
      `organization-trusted-intermediary-private-key-<environment>.pem` private key in Keybase as the signing key.
   4. Click Create.
   5. Drill into the latest version of this secret, and click the copy to clipboard button in the Secret Identifier
      textbox.  We will be using this later during the creation of the actual load test.
   6. Navigate to the Access policies slice, and click Create.  Select Get and List for Secrets for the permissions and
      the name of the previously created load test as the principal.
5. Navigate to the Tests slice, which is under the Tests group, of the previously created load test and click Create and then Upload a script to start
   walking through the wizard.
   1. Under the Test plan tab...
      1. Select the Locust radio button.
      2. Upload the [`./operations/locustfile.py`](./operations/locustfile.py) file.
      3. Additional data files from our repository used by the load test need to be uploaded.  E.g. order and result
         FHIR files.  You can inspect the `locustfile.py` file to find out which data files are used.  As of this
         writing, that is `002_ORM_O01_short.fhir` and `001_ORU_R01_short.fhir`.
   2. Under the Parameters tab, add a secret with `trusted-intermediary-valid-token-jwt` as the name.  The Value is the
      secret URL referenced previously when you added the secret JWT to the key vault.  Before pasting the secret,
      remove the hexadecimal version from the end of the URL.  E.g.
      `https://<key-vault>.vault.azure.net/secrets/trusted-intermediary-valid-token-jwt/cf7eb05481c449878f2afe6b51464fd5`
      becomes `https://<key-vault>.vault.azure.net/secrets/trusted-intermediary-valid-token-jwt/`.  We always want to
      reference the last version, and we can do so by omitting the specific version.
   3. Under the Load tab, configure how much load you want.  You also need to provide the URL of the application you
      want to load test.
   4. Under the Test criteria tab, fill in any client-side metrics that you want to evaluate at the end of the load
      test.  This makes it easy to tell whether the application has the performance we want.  Consider whether you want
      the test to automatically stop if there are too many errors.
   5. Create the test.  All the other options not covered here should be looked at and considered.

### Debugging

#### Attached JVM Config for IntelliJ

The project comes with an attached remote jvm configuration for debugging the container.
If you check your remote JVM settings, under `Run/Edit Configurations`,
you will see the `Debug TI`. If you want to add a new remote JVM configuration, follow the steps below,
under "**Docker Container Debugging Using Java Debug Wire Protocol**"

#### Docker Container Debugging Using Java Debug Wire Protocol (JDWP)

Go into the `Dockerfile` file and change `CMD ["java", "-jar", "app.jar"]` to `CMD ["java", "-agentlib:jdwp=transport=dt_socket,address=*:6006,server=y,suspend=n", "-jar", "app.jar"]`

#### Steps

1. In Intellij, click on Run and select Edit Configurations ![img.png](images/img.png)
2. Create a new Remote JVM Debug ![img_1.png](images/img_1.png)
3. Set up the configuration for the remote JVM debug to look like this. ![img_3.png](images/img_2.png)
4. In your code, set your breakpoint, and then start your docker container with `docker-compose up --build`
5. Once your docker container is running, in order to attach, select Run again.
6. Select Debug (not Attach to Process) ![img_3.png](images/img_3.png)
7. Select your Docker Debug that you set up in step 3 ![img_4.png](images/img_4.png)
8. A console window will pop up that will show you that it is connected to Docker, and at that point, you can interact with your container and then step through the code at your breakpoints. ![img_5.png](images/img_5.png)

### Deploying

#### Environments

We have a number of environments that are split between CDC and non-CDC Azure Entra domains and subscriptions.

##### Internal

The Internal environment is designed to be the Wild West, meaning anyone can push changes without restrictions. It allows for testing various configurations without the requirement that only stable builds be pushed.  Use the Internal environment if you want to test something in a
deployed environment in a _non-CDC_ Azure Entra domain and subscription. See below:

> **Before starting...**
>
> Remember to ping the Engineering Channel to make sure someone is not already using the enviroment.

To deploy to the Internal environment...

1. Check with the team that no one is already using it.
2. [Find the `internal` branch](https://github.com/CDCgov/trusted-intermediary/branches/all?query=internal) and delete
   it inGitHub.
3. Delete your local `internal` branch if needed.

   ```shell
   git branch -D internal
   ```

4. From the branch you want to test, create a new `internal` branch.

   ```shell
   git checkout -b internal
   ```

5. Push the branch to GitHub.

   ```shell
   git push --set-upstream origin internal
   ```

Then the [deploy](https://github.com/CDCgov/trusted-intermediary/actions/workflows/internal-deploy.yml) will run.
Remember that you now have the `internal` branch checked out locally.  If you make subsequent code changes, you will
make them on the `internal` branch instead of your original branch.

##### Dev

The Dev environment is similar to the Internal environment but deploys to a CDC Azure Entra domain and subscription.  It
is also meant to be the Wild West.  Dev deploys similarly to the Internal environment, but you interact with the
`dev` branch.

##### Staging

The Staging environment is production-like and meant to be stable.  It deploys to a CDC Azure Entra domain and
subscription.  Deployments occur when a commit is made to the `main` branch.  `main` is a protected branch and requires
PR reviews before merge.

##### Prod

The Production environment is the real deal.  It deploys to a CDC Azure Entra domain and subscription.  Deployments
occur when a release is published.

#### Initial Azure and GitHub Configuration

There is minimal set-up to do to get Terraform setup before you can run the Terraform commands in
a new Azure environment in the Flexion Entra domain.  For example, the `internal` environment.  This does not apply to the CDC
Entra domains and subscriptions.

1. Create a resource group.
2. Create a storage account inside the aforementioned resource group.
3. Within the new storage account, create a Container.
4. Within Azure Entra...
   1. Create an App Registration.
   2. Add federated credentials to the App Registration
      - `repo:CDCgov/trusted-intermediary:ref:refs/heads/main` (for terraform apply).
      - `repo:CDCgov/trusted-intermediary:environment:staging` (for staging webapp deploy).
      - And presumably other repo paths needed in the future for other environments and branches.
   3. Within your Subscription, assign the Contributor role to the previously created App Registration.
5. Add GitHub Action secrets to your GitHub repository.
   - A secret with the tenant ID from Azure Entra directory.
   - A secret with the ID from the subscription that everything should be deployed into.
   - A secret with the ID of the App Registration created previously.
6. Create a copy of one of the environments under the [operations](./operations) folder.
   1. Name the copy off of the name of the new environment. Ex: `internal`
   2. Edit the `main.tf` file with the names of the resources previously created: `resource_group_name`,
      `storage_account_name`, `container_name`.  Also update the `environment` to match the new folder name.
7. Create a GitHub Action workflow so that automatic deploys can occur.  You can take inspiration from our
   [Internal environment deployment](./.github/workflows/internal-deploy.yml).  Make sure you set the `AZURE_CLIENT_ID`,
   `AZURE_TENANT_ID`, and `AZURE_SUBSCRIPTION_ID` based on the secrets created previously.

#### Interacting with Deployed Environments

The PR and Internal environment is available on the public Internet and able to be interacted with directly.

The Dev, Staging, and Prod environment are deployed inside a Vnet and require special steps to interact with these.

##### Application

The application basically has a firewall in place.  You need to add (and remove when you're done) your IP address to the
firewall allow list.

1. Log into CyberArk and then into Azure with your -SU account.
2. Navigate to the environment's app service.
3. Click on Networking in the left pane.
4. Click on the "Enabled with access restrictions" link under "Inbound traffic configuration".
5. Add a new rule to allow your _public_ IP address.  Provide an appropriate name with your name.  The priority will
   need a lower number than the existing denies.  It will look like your IP address with a `/32` appended.  E.g.
   `192.168.0.1/32`.
6. Click "Save".

You will now be able to interact with that environment's application.
> **Note:**
>
> Don't forget to remove your rule and save when
you are done.

##### Database

You will need to connect to the VPN for the given environment first, and then you can interact with the database.
Notion contains the
[instructions for connecting to the VPN](https://www.notion.so/flexion-cdc-ti/Azure-VPN-pieces-d814ddcb87b1467f93ccf473e9cdb69c?pvs=4).
After connecting, you can follow the [database documentation](docs/database.md) to gain access.

### Pre-Commit Hooks

We use [`pre-commit`](https://pre-commit.com) to run [some hooks](./.pre-commit-config.yaml) on every commit.  These
hooks do linting to ensure things are in a good spot before a commit is made.  Please install `pre-commit` and then
install the hooks.

```shell
pre-commit install
```

### Contributing

Anyone is encouraged to contribute to the repository by [forking](https://help.github.com/articles/fork-a-repo)
and submitting a pull request. (If you are new to GitHub, you might start with a
[basic tutorial](https://help.github.com/articles/set-up-git).) By contributing
to this project, you grant a world-wide, royalty-free, perpetual, irrevocable,
non-exclusive, transferable license to all users under the terms of the
[Apache Software License v2](http://www.apache.org/licenses/LICENSE-2.0.html) or
later.

Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) for additional details.

All comments, messages, pull requests, and other submissions received through
CDC including this GitHub page may be subject to applicable federal law, including but not limited to the Federal Records Act, and may be archived. Learn more at [http://www.cdc.gov/other/privacy.html](http://www.cdc.gov/other/privacy.html).

### Database

For database documentation: [/docs/database.md](/docs/database.md)

### Setup with ReportStream

#### CDC-TI Setup

1. Checkout `main` branch for `CDCgov/trusted-intermediary`
2. Run `./generate_env.sh` to generate `.env` file with required environment variables
3. Run TI with `./gradlew clean run`

#### ReportStream Setup

For Apple Silicon users, please make sure the Docker/Podman option to use `Rosetta` is enabled. If it was disabled, after enabling it is recommended that you delete all images and containers and rebuild them with this option enabled.

1. Checkout `main` branch for `CDCgov/prime-reportstream`
2. Build and package RS (for more information please refer to the [ReportStream docs](https://github.com/CDCgov/prime-reportstream/blob/master/prime-router/docs/getting-started/README.md))
   - If building for the first time:
      - Run: `./cleanslate.sh` in `prime-reportstream/prime-router/`
      - **Note**: if you're using an Apple Silicon computer, before running the script edit `cleanslate.sh` to comment out the following lines:

         ```
         if [ "$(uname -m)" = "arm64" ] && [[ $(uname -av) == _"Darwin"_ ]]; then
            PROFILE=apple_silicon
            SERVICES=(sftp azurite vault) # Only these services are M1 compatible
            BUILD_SERVICES=(postgresql)
         fi
         ```

   - If not building for the first time:
      1. Make sure no `prime-router` containers are running. If they are, stop them
      2. Run: `docker compose -f docker-compose.build.yml up -d` in `prime-reportstream/prime-router/`
      3. Run: `./gradlew clean quickPackage` in `prime-reportstream/`
         - **Note**: if the command fails, try removing the `.gradle` folder in `prime-reportstream/`: `rm -rf .gradle`
         - **Note**: if attempting to access the metadata endpoint in RS, add the variable `ETOR_TI_baseurl="http://host.docker.internal:8080"` to `prime-router/.vault/env/.env.local` file before running
3. Run RS with gradle: `./gradlew quickRun`
4. Run the RS setup script in this repository: `/scripts/setup/setup-reportstream.sh`
   - Before running the script, make sure to follow the instructions in [/scripts/README.md](/scripts/README.md)
   - You can verify the script created vault secrets successfully by going to `http://localhost:8200/` in your browser, use the token in `prime-router/.vault/env/.env.local` to authenticate, and then go to `Secrets engines` > `secret/` to check the available secrets

#### Submit request to ReportStream

We have a `submit.sh` script that simplifies the process of preparing, sending and tracking messages in [scripts/](scripts/). Otherwise, you'll find instructions below on how to send messages using `curl`

##### Locally

###### Orders

To test sending a HL7 order from a simulated hospital:

```
curl --header 'Content-Type: application/hl7-v2' --header 'Client: flexion.simulated-hospital' --header 'Authorization: Bearer dummy_token' --data-binary '@/path/to/orm_message.hl7' 'http://localhost:7071/api/waters'
```

To test sending a FHIR order from TI:

```
curl --header 'Content-Type: application/fhir+ndjson' --header 'Client: flexion.etor-service-sender' --header 'Authorization: Bearer dummy_token' --data-binary '@/path/to/oml_message.fhir' 'http://localhost:7071/api/waters'
```

###### Results

To test sending an HL7 result from a simulated lab:

```
curl --header 'Content-Type: application/hl7-v2' --header 'Client: flexion.simulated-lab' --header 'Authorization: Bearer dummy_token' --data-binary '@/path/to/oru_message.hl7' 'http://localhost:7071/api/waters'
```

To test sending a FHIR result from TI:

```
curl --header 'Content-Type: application/fhir+ndjson' --header 'Client: flexion.etor-service-sender' --header 'Authorization: Bearer dummy_token' --data-binary '@/path/to/oru_message.fhir' 'http://localhost:7071/api/waters'
```

After one or two minutes, check that hl7 files have been dropped to `prime-reportstream/prime-router/build/sftp` folder

##### Staging

In order to submit a request, you'll need to authenticate with ReportStream using JWT auth:

1. Create a JWT for the sender (e.g. `flexion.simulated-hospital`) using the sender's private key, which should be stored in Keybase. You may use [this CLI tool](https://github.com/mike-engel/jwt-cli) to create the JWT:

   ```
   jwt encode --exp='+5min' --jti $(uuidgen) --alg RS256 -k <sender> -i <sender> -s <sender> -a staging.prime.cdc.gov --no-iat -S @/path/to/sender_private.pem
   ```

2. Use the generated JWT to authenticate with ReportStream and get the token, which will be in the `access_token` response

   ```
   curl --header 'Content-Type: application/x-www-form-urlencoded' --data 'scope=flexion.*.report' --data 'client_assertion=<jwt>' --data 'client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer' --data 'grant_type=client_credentials' 'http://localhost:7071/api/token'
   ```

3. Submit an Order or Result using the returned token in the `'Authorization: Bearer <token>'` header

## DORA Metrics

We use [DORA Metrics](https://cloud.google.com/blog/products/devops-sre/using-the-four-keys-to-measure-your-devops-performance) to measure our DevOps performance. We currently are tracking Deployment Frequency, Change Fail Rate and Mean Time to Recovery.

The metrics are produced weekly using a [Github Action](https://github.com/CDCgov/trusted-intermediary/actions/workflows/metrics.yml) and written into CSV files which are available for download in the [workflow job's artifacts]((https://github.com/CDCgov/trusted-intermediary/actions/workflows/metrics.yml)).

## Related Documents

- [Open Practices](open_practices.md)
- [Rules of Behavior](rules_of_behavior.md)
- [Thanks and Acknowledgements](thanks.md)
- [Disclaimer](DISCLAIMER.md)
- [Contribution Notice](CONTRIBUTING.md)
- [Code of Conduct](code-of-conduct.md)

## CDC Notices

### Public Domain Standard Notice

This repository constitutes a work of the United States Government and is not
subject to domestic copyright protection under 17 USC § 105. This repository is in
the public domain within the United States, and copyright and related rights in
the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
All contributions to this repository will be released under the CC0 dedication. By
submitting a pull request you are agreeing to comply with this waiver of
copyright interest.

### License Standard Notice

The repository utilizes code licensed under the terms of the Apache Software
License and therefore is licensed under ASL v2 or later.

This source code in this repository is free: you can redistribute it and/or modify it under
the terms of the Apache Software License version 2, or (at your option) any
later version.

This source code in this repository is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the Apache Software License for more details.

You should have received a copy of the Apache Software License along with this
program. If not, see <http://www.apache.org/licenses/LICENSE-2.0.html>

The source code forked from other open source projects will inherit its license.

### Privacy Standard Notice

This repository contains only non-sensitive, publicly available data and
information. All material and community participation is covered by the
[Disclaimer](DISCLAIMER.md) and [Code of Conduct](code-of-conduct.md).
For more information about CDC's privacy policy, please visit [http://www.cdc.gov/other/privacy.html](https://www.cdc.gov/other/privacy.html).

### Records Management Standard Notice

This repository is not a source of government records, but is a copy to increase
collaboration and collaborative potential. All government records will be
published through the [CDC website](http://www.cdc.gov).

### Additional Standard Notices

Please refer to [CDC's Template Repository](https://github.com/CDCgov/template)
for more information about [contributing to this repository](https://github.com/CDCgov/template/blob/master/CONTRIBUTING.md),
[public domain notices and disclaimers](https://github.com/CDCgov/template/blob/master/DISCLAIMER.md),
and [code of conduct](https://github.com/CDCgov/template/blob/master/code-of-conduct.md).

### Troubleshooting

Error: copier: stat: "/app/build/libs/app-all.jar": no such file or directory
Solution: Run ./gradlew shadowjar first
