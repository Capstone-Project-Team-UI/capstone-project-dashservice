# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:23-jdk-alpine

# Set the working directory inside the container
WORKDIR /app
# Copy the .env file into the container's /app directory
COPY ./.env /app/

# Copy the jar file into the container
COPY target/remote-pc-control-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your application runs on
EXPOSE 8090

# Set the entry point to run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
