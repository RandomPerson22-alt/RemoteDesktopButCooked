# ---- Stage 1: Build ----
FROM gradle:8.3-jdk17 AS builder

WORKDIR /app
COPY . .

# Build ONLY the server fat JAR
RUN gradle :server:fatJar --no-daemon --stacktrace

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre

WORKDIR /app

EXPOSE 8080

# Copy the built jar (assuming only one jar in libs)
COPY --from=builder /app/server/build/libs/*.jar /app/server.jar

# Run the server
CMD ["java", "-jar", "server.jar"]
