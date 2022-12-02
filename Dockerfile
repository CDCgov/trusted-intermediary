# Use Linux-Alpine image
FROM openjdk:17-alpine3.14

# Create directory and switch to it
WORKDIR /app

# Add project to created folder
ADD . .

# Run the api
CMD ["./gradlew", ":app:run"]

# Use port 8080
EXPOSE 8080
