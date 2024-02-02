# Use Linux-Alpine image
FROM amazoncorretto:17.0.10-alpine

ARG JAR_LIB_FILE=./app/build/libs/app-all.jar

# Create directory and switch to it
WORKDIR /app

# Add application JAR to created folder
COPY ${JAR_LIB_FILE} app.jar

# Run the api
CMD ["java", "-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n", "-jar", "app.jar"]

# Use port 8080
EXPOSE 8080
