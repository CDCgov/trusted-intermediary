FROM khipu/openjdk17-alpine
WORKDIR /app
# ADD .gradle .
# ADD gradle .
ADD . .
CMD ["./gradlew", ":app:run"]
