# syntax=docker/dockerfile:1
FROM alpine:3.18.5
RUN apk update && apk add bash curl openjdk17 ruby-dev ruby-bundler ruby-jekyll npm git msttcorefonts-installer fontconfig && update-ms-fonts
RUN cp `find /usr/lib/ruby/gems/*/gems/jekyll-*/exe/jekyll` /usr/bin
RUN npm install -g fsh-sushi

WORKDIR /trusted-intermediary

COPY input/ ./input/
COPY ig.ini sushi-config.yaml .
COPY --chmod=755 _updatePublisher.sh .
RUN ./_updatePublisher.sh -y && rm *.bat && ./_genonce.sh
