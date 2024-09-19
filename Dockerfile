# Use a more complete OpenJDK runtime as a parent image
FROM eclipse-temurin:22-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/
COPY src /app/src

# Ensure gradlew has execution permissions
RUN chmod +x /app/gradlew

# Build the application using the Gradle wrapper
RUN ./gradlew build --no-daemon

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the application directly without extracting the jar
ENTRYPOINT ["java", "-jar", "build/libs/File-Manager-0.0.1-SNAPSHOT.jar"]