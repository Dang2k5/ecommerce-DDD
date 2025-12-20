# syntax=docker/dockerfile:1.6

############################################
# Build stage (JDK 25 + Maven Wrapper)
############################################
ARG JAVA_VERSION=25
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS build

WORKDIR /app

# 1) Copy wrapper + pom trước để cache dependency
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
 && ./mvnw -q -DskipTests dependency:go-offline

# 2) Copy source sau
COPY src/ src/

# 3) Build jar
RUN ./mvnw -q -DskipTests package

# 4) Chuẩn hoá tên jar để runtime COPY không dính nhiều file (*-original.jar)
RUN set -eux; \
    JAR="$(ls -1 target/*.jar | grep -v 'original' | head -n 1)"; \
    test -n "$JAR"; \
    cp "$JAR" /app/app.jar


############################################
# Runtime stage (JRE 25, non-root)
############################################
FROM eclipse-temurin:${JAVA_VERSION}-jre

WORKDIR /app

# Chạy non-root cho K8s
RUN useradd -m -u 10001 appuser
USER 10001

# Copy jar đã chuẩn hoá
COPY --from=build /app/app.jar /app/app.jar

# Spring Boot port trong application.yaml của bạn là 8086
EXPOSE 8086

# Gợi ý: truyền cấu hình qua ENV ở K8s (ConfigMap/Secret)
# Ví dụ (K8s):
# -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/payment_service
# -e SPRING_DATASOURCE_USERNAME=payment_user
# -e SPRING_DATASOURCE_PASSWORD=xxxx
# -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV JAVA_OPTS=""

# Chạy app (cho phép set JAVA_OPTS trong CI/CD/K8s)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
