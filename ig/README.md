# Implementation Guide Documentation

The documentation for this project is kept in an HL7 FHIR style Implementation Guide.  It uses Markdown files plus FHIR Shorthand (FSH) to generate the documentation as plain HTML files that can be hosted anywhere.

## Pre-requisites

- A Java JDK - The full JDK is required.  Must be Java 17+.
- The latest stable Ruby
- Jekyll
- Git
- [SUSHI](https://fshschool.org/docs/sushi/installation/)

## Implementation Guide Basics

To be written

## Generate Implementation Guide

1. In a terminal, navigate to this `ig` directory
2. run `./_updatePublisher.sh` in order to ensure you are using the most recent publisher release
3. run `./_genonce.sh` to generate the Implementation Guide
4. In a browser you can open up the `ig/output/index.html` file that was generated as this is the home page for the Implementation Guide
5. Alternately, you may opt to use the local development Docker approach outlined below to avoid having to install the toolchain listed above.

## Using Docker

A Dockerfile is provided that can be used to generate the Implementation Guide.  This is used to generate the documentation in the CI/CD process.

1. In a terminal, navigate to this `ig` directory
2. run `docker buildx build -t ig-test .` to build the docker container
3. run `docker run -it ig-test bash` to run the container and use an interactive `bash` shell inside the container
4. The container already has updated tools when you built it
5. The container also pre-generates the documentation.  This process could make the image build take 10-15 minutes
6. All the generated documentation should be in the `/trusted-intermediary/output` directory
7. If building locally, you may want to add a `--no-cache` to the `docker buildx build` command so that the documentation generation is not cached

## Using Docker for local development

Provided is a Dockerfile suitable for running the implementation guide documentation generation without the need to install all the tooling locally.
The Docker image needs to be built and then run with an appropriate `mount` flag in order to mount your local `ig` directory into the running Docker container.
This will allow bidirectional writes so that the Docker container can generate the documentation, and as a developer you can view the generated documentation easily.
A developer will also be able to update any input files for the documentation and re-run the documentation generation without any intermediate steps.

In practice, when generating the documentation, VSCode is unable to cope with refreshing the `output` directory.  During and after documentation generation
VSCode may use significant amounts of your CPU, locking up your computer and will typically crash.  To avoid this issue, it is recommended that a different
IDE is used to work on the CDC Trusted Intermediary documentation such as IntelliJ.

1. In a terminal, navigate to this `ig` directory
2. run `docker build -t ig-local-dev -f Dockerfile-local-dev .` to build the Docker container
3. run `docker run -it --mount type=bind,source="$(pwd)",target=/trusted-intermediary ig-local-dev bash` to run the container and use an interactive `bash` shell inside the container
4. Inside the container environment it should default to a working directory of `/trusted-intermediary`; this is bound to the `ig` directory in your local git repo
5. Periodically update the IG Publisher by running `./_updatePublisher.sh`.  The IG Publisher is updated multiple times per week to fix bugs and add new functionality.  Staying up to date with the latest version is a good idea. Notification messages when the IG Publisher is updated can be found at: https://chat.fhir.org/#narrow/stream/217600-tooling.2Freleases
6. After running the publisher update, you will have to fix the execute permission on a couple of the scripts by running `./fix-execute.sh`
7. In order to build the documentation, run `./_genonce.sh`
8. You can browse the generated documentation by opening in a browser `file:///path/to/repo/ig/output/index.html`

## Remove cached layers

Sometimes you want to build a container from a clean slate.  To remove any cached items and layers in the Docker build system, run `docker system prune -af`
Be aware that this could remove other unused containers and images that you would rather keep.
