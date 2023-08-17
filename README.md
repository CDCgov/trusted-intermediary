# CDC Trusted Intermediary

## Requirements

Any distribution of the Java 17 JDK.

## Using and Running

To run the application directly, execute...

```shell
./gradlew clean run
```

This will run the web API on port 8080.  You can view the API documentation at `/openapi`.

## Development

### Additional Requirements

The additional requirements needed to contribute towards development are...

- [Pre-Commit](https://pre-commit.com)
- [Locust.io](https://docs.locust.io/en/stable/installation.html)
- [Python](https://docs.python-guide.org/starting/installation/)
- [Terraform](https://www.terraform.io)

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
./gradlew app:clean app:test
```

#### End-to-end Tests

End-to-end tests are meant to interact and assert the overall flow of the API is operating correctly. They require that the API to be running already.

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

#### Load Testing

Load tests are completed with [Locust.io](https://docs.locust.io/en/stable/installation.html).  Run the load tests by
running...

```shell
./load-execute.sh
```

This will run the API for you, so no need to run it manually.

The `locustfile.py` that specifies the load test is located at
[`./operations/locustfile.py`](./operations/locustfile.py).

If you want to run the load test in an interactive mode, run...

```shell
locust -f ./operations/locustfile.py
```

The terminal will start a local web interface, and you can enter
the swarm parameters for the test and the local url where the app is running
(usually http://localhost:8080).  You can also set time limits for the tests under 'Advanced Settings'.

### Deploying

#### Initial Azure and GitHub Configuration

There is minimal set-up to do to get Terraform squared away before you can run the Terraform commands in
a new Azure environment.

1. Create a resource group: `cdcti-terraform`.
2. Create a storage account: `cdctiterraform` (with `cdcti-terraform` as the resource group).
3. Within the new storage account, create a Container named "tfstate"
4. Within Azure Active Directory...
   - Create an App Registration: `cdcti-github`
   - Within your Subscription, create a Service Account and assign the Contributor role
   - Add federated credentials for:
     - `repo:CDCgov/trusted-intermediary:ref:refs/heads/main` (for terraform apply)
     - `repo:CDCgov/trusted-intermediary:environment:staging` (for staging webapp deploy)
     - And presumably other repo paths needed in the future for other environments
5. Add secrets to your GitHub Actions.
   - `AZURE_TENANT_ID` with the tenant ID from Azure Active Directory.
   - `AZURE_SUBSCRIPTION_ID` with the ID from the subscription that everything should be deployed into.
   - `AZURE_CLIENT_ID` with the ID of the App Registration created previously.

#### Dev Environment Deployment

The Dev environment is meant to be the Wild West.  Meaning anyone can push to it to test something, and there is no
requirement that only good builds be pushed to it.  Use the Dev environment if you want to test something in a deployed
environment.

To deploy to the Dev environment...
1. Check with the team that no one is already using it.
2. [Find the `dev` branch](https://github.com/CDCgov/trusted-intermediary/branches/all?query=dev) and delete it in
   GitHub.
3. Delete your local `dev` branch if needed.
   ```shell
   git branch -D dev
   ```
4. From the branch you want to test, create a new `dev` branch.
   ```shell
   git checkout -b dev
   ```
5. Push the branch to GitHub.
   ```shell
   git push --set-upstream origin dev
   ```

Then the [deploy](https://github.com/CDCgov/trusted-intermediary/actions/workflows/dev-deploy.yml) will run.  Remember
that you now have the `dev` branch checked out locally.  If you make subsequent code changes, you will make them on the `dev`
branch instead of your original branch.

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

### Setup with ReportStream

#### CDC-TI Setup

1. Checkout `rs-form-data` branch for `CDCgov/trusted-intermediary`
2. Run TI with `REPORT_STREAM_URL_PREFIX=http://localhost:7071/ ./gradlew clean app:run`

#### ReportStream Setup

1. Checkout `flexion/test/ti-rs-setup` branch for `CDCgov/prime-reportstream`
2. CD to `prime-reportstream/prime-router`
3. Point to RS docs to run RS
4. Run RS with `docker compose up --build -d`
5. Run `./prime multiple-settings set -i ./settings/staging/0149-etor.yml`
6. Run `./prime organization addkey --public-key /path/to/trusted-intermediary/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit`
7. Setup local vault secret
    1. Go to: `http://localhost:8200/`
    2. Use token in `prime-router/.vault/env/.env.local` to authenticate
    3. Go to `Secrets engines` > `secret/` > `Create secret`
        1. Path for this secret: `FLEXION--ETOR-SERVICE-RECEIVER`
        2. JSON data:
        ```
        {
          "@type": "UserApiKey",
          "apiKey": "TI's private key in RS at trusted-intermediary/mock_credentials/organization-report-stream-private-key.pem",
          "user": "flexion"
        }
        ```

#### Submit request to ReportStream

`curl --header 'Content-Type: application/hl7-v2' --header 'Client: flexion.simulated-hospital' --header 'Authorization: Bearer none' --data-binary '@/path/to/ORM_O01.hl7' 'http://localhost:7071/api/reports'`

or

`curl --header 'Content-Type: application/fhir+ndjson' --header 'Client: flexion.etor-service-sender' --header 'Authorization: Bearer none' --data-binary '@/path/to/lab_order.json' 'http://localhost:7071/api/reports'`

After one or two minutes, check that hl7 files have been dropped to `prime-reportstream/prime-router/build/sftp` folder

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
program. If not, see http://www.apache.org/licenses/LICENSE-2.0.html

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
