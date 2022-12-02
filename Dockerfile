# Use Linux-Alpine image
FROM khipu/openjdk17-alpine

# Create directory and switch to it
WORKDIR /app

# cache
#ADD /.gradle .
#ADD /gradle .

ADD . .
CMD ["./gradlew", ":app:run"]
