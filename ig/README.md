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

- In a terminal, navigate to this `ig` directory
- run `./_updatePublisher.sh` in order to ensure you are using the most recent publisher release
- run `./_genonce.sh` to generate the Implementation Guide
- In a browser you can open up the `ig/output/index.html` file that was generated as this is the home page for the Implementation Guide

## Using Docker

A Dockerfile is provided that can be used to generate the Implementation Guide.

- In a terminal, navigate to this `ig` directory
- run `docker build -t ig-test .` to build the docker container
- run `docker run -i -t ig-test bash` to run the container and use an interactive `bash` shell inside the container
- The container already has updated tools when you built it
- run `./_genonce.sh` inside the container to build the Implementation Guide as usual
