# Use lightweight Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/*.jar app.jar

# Expose port (Render will override with PORT)
EXPOSE 8080

# Run application
ENTRYPOINT ["java","-jar","app.jar"]