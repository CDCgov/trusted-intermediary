FROM amazoncorretto:17.0.12-alpine

RUN apk update && apk -U upgrade && rm -rf /var/cache/apk/*

RUN adduser -S myLowPrivilegeUser
USER myLowPrivilegeUser

# Set the workdir to a location that the running application can write to
# which is in the myLowPrivilegeUser home folder because we are running as that user instead of root.
WORKDIR /home/myLowPrivilegeUser/app/

# Copy the jar file into /usr/local/bin/ because it seemingly needs to go to a location that any user can access.
# If we put the jar file into the myLowPrivilegeUser's home directly, the container fails to run in Azure.
COPY --chown=myLowPrivilegeUser ./app/build/libs/app-all.jar /usr/local/bin/app.jar

CMD ["java", "-jar", "/usr/local/bin/app.jar"]

EXPOSE 8080
