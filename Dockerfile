# Use Linux-Alpine image
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# Create directory and switch to it
WORKDIR /app

# Add project to created folder
ADD . .

# Run the api
CMD ["./gradlew", ":app:run"]

# Use port 8080
EXPOSE 8080
