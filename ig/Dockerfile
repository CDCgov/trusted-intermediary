# syntax=docker/dockerfile:1
FROM alpine:3.18.0
RUN apk update && apk add bash curl openjdk17 ruby-dev ruby-bundler ruby-jekyll npm git msttcorefonts-installer fontconfig
RUN update-ms-fonts
COPY ./findandcopy.sh .
RUN chmod +x ./findandcopy.sh

SHELL ["bin/bash", "-c"]

RUN ./findandcopy.sh
RUN npm install -g fsh-sushi
COPY . trusted-intermediary/
#RUN ./trusted-intermediary/_updatePublisher.sh
#CMD ./trusted-intermediary/_genonce.sh
