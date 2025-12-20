# syntax=docker/dockerfile:1.7

##
## BUILD STAGE
##
ARG JAVA_VERSION=25
FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy AS build

WORKDIR /workspace

# Copy maven wrapper + pom trước để tận dụng cache tốt cho CI/CD
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Cache Maven dependencies (rất lợi khi build trên CI)
RUN chmod +x mvnw && \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw -DskipTests dependency:go-offline

# Copy source sau cùng
COPY src ./src

# Build jar
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -DskipTests package


##
## RUNTIME STAGE
##
FROM eclipse-temurin:${JAVA_VERSION}-jre-jammy

# Chạy non-root để hợp chuẩn security trên Kubernetes
RUN groupadd -g 10001 app && useradd -u 10001 -g 10001 -m -s /usr/sbin/nologin app

WORKDIR /app

# Copy jar từ build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Set permission cho user non-root
RUN chown -R app:app /app

USER app

EXPOSE 8082

# K8s có thể set JAVA_TOOL_OPTIONS (khuyến nghị) hoặc SPRING_* env tuỳ ý
ENV JAVA_TOOL_OPTIONS=""

# Exec form (không dùng sh -c) để nhận signal tốt (SIGTERM) trong K8s
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
