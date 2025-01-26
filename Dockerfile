# Use OpenJDK as the base image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the fat JAR from the build folder
COPY build/libs/characterai_bot.jar app.jar

# Command to run the app
CMD ["java", "-jar", "app.jar"]