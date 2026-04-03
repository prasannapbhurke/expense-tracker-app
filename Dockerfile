# Use an Eclipse Temurin OpenJDK 8 image as a parent image
FROM eclipse-temurin:8-jdk-focal

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew .
COPY gradle ./gradle

# Copy the build configuration files
COPY build.gradle .
COPY settings.gradle .

# Copy the source code for both the app and backend modules
COPY app ./app
COPY backend ./backend

# Make the Gradle wrapper executable
RUN chmod +x ./gradlew

# Build the backend application. The --no-daemon flag is good practice for CI environments.
RUN ./gradlew :backend:bootJar --no-daemon

# Expose port 8080 to the outside world
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java","-jar","backend/build/libs/backend-0.0.1-SNAPSHOT.jar"]
