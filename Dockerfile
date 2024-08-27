# Use Linux-Alpine image
FROM amazoncorretto:17.0.12-alpine

RUN apk update && apk -U upgrade && rm -rf /var/cache/apk/*

RUN adduser -S myLowPrivilegeUser
USER myLowPrivilegeUser

ARG JAR_LIB_FILE=./app/build/libs/app-all.jar

# Create directory and switch to it
#WORKDIR /home/myLowPrivilegeUser/app
WORKDIR /usr/local/bin/

# Add application JAR to created folder
COPY --chown=myLowPrivilegeUser ${JAR_LIB_FILE} app.jar

# Run the api
CMD ["java", "-jar", "app.jar"]

# Use port 8080
EXPOSE 8080
