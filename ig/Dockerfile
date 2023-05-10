# syntax=docker/dockerfile:1
FROM alpine:3.17.3
RUN apk update && apk add bash curl openjdk17 ruby-dev ruby-bundler ruby-jekyll npm git msttcorefonts-installer fontconfig
RUN update-ms-fonts
RUN cp /usr/lib/ruby/gems/3.1.0/gems/jekyll-4.3.1/exe/jekyll /usr/bin
RUN npm install -g fsh-sushi
COPY . trusted-intermediary/
RUN ./trusted-intermediary/_updatePublisher.sh
RUN ./trusted-intermediary/_genOnce.sh
