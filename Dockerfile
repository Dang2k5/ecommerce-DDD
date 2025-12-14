# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy maven wrapper trước để cache dependency tốt hơn
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw -DskipTests dependency:go-offline

# Copy source và build
COPY src src
RUN ./mvnw -DskipTests package

# Runtime
FROM eclipse-temurin:25-jdk
WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8081
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
