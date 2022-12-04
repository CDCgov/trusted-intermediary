# Use openjdk image that will work with m1 chip architectrue
FROM arm64v8/openjdk:17

# Create directory and switch to it
WORKDIR /app

# Add project to created folder
ADD . .

# Run the api
CMD ["./gradlew", ":app:run"]

# Use port 8080
EXPOSE 8080
