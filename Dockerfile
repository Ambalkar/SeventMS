# Dockerfile for SEVENT-MS deployment on AWS App Runner, Elastic Beanstalk, or ECS
# Stage 1: Builder
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -B
COPY src ./src
COPY frontend ./frontend
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/SeventMS.war app.war

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom -jar app.war"]

