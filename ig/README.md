# Implementation Guide Documentation

The documentation for this project is kept in an HL7 FHIR style Implementation Guide.  It uses Markdown files plus FHIR Shorthand (FSH) to generate the documentation as plain HTML files that can be hosted anywhere.

## Pre-requisites

- A Java JDK - The full JDK is required.  Must be Java 17+.
- The latest stable Ruby
- Jekyll
- Git
- [SUSHI](https://fshschool.org/docs/sushi/installation/)

## Implemenation Guide Basics

To be written

## Generate Implementation Guide

1. In a terminal, navigate to this `ig` directory
1. run `./_updatePublisher.sh` in order to ensure you are using the most recent publisher release
1. run `./_genonce.sh` to generate the Implementation Guide
1. In a browser you can open up the `ig/output/index.html` file that was generated as this is the home page for the Implementation Guide

## Using Docker

A Dockerfile is provided that can be used to generate the Implementation Guide.

1. In a terminal, navigate to this `ig` directory
1. run `docker buildx build -t ig-test .` to build the docker container
1. run `docker run -it ig-test bash` to run the container and use an interactive `bash` shell inside the container
1. The container already has updated tools when you built it
1. The container also pre-generates the documentation.  This process could make the image build take 10-15 minutes
1. All of the generated documentation should be in the `/trusted-intermediary/output` directory
1. If building locally, you may want to add a `--no-cache` to the `docker buildx build` command so that the documentation generation is not cached
