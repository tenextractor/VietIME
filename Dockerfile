FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache unzip

WORKDIR /workspace

# Copy Gradle wrapper first for caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle* settings.gradle* ./

# Pre-fetch Gradle + Kotlin
RUN ./gradlew --version

# Copy the rest of the project
COPY . .

# Default command is overridden in docker-compose
CMD ["./gradlew", "build"]
