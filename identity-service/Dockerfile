# syntax=docker/dockerfile:1.7

############################
# 1) Build stage
############################
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy maven wrapper & pom trước để cache dependency tốt
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw

# Tải dependency trước (dùng cache .m2 để CI/CD build nhanh hơn)
# NOTE: dùng -Dmaven.test.skip=true để KHÔNG compile test luôn (tránh lỗi thiếu spring-boot-test như saga-messages)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Dmaven.test.skip=true dependency:go-offline

# Copy toàn bộ source rồi build
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Dmaven.test.skip=true clean package

############################
# 2) Runtime stage
############################
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Chạy non-root (hợp chuẩn security baseline khi deploy K8s)
RUN useradd -r -u 10001 -g root -s /sbin/nologin appuser

# Copy jar từ build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Set quyền cho user non-root
RUN chown -R 10001:0 /app && chmod -R g=u /app

USER 10001

# Identity-service của bạn chạy port 8081 (bạn vẫn set SERVER_PORT trong K8s thì EXPOSE cũng không bắt buộc)
EXPOSE 8081

# JAVA_OPTS để bạn truyền thêm flags từ Helm/K8s (resources, GC, profile, v.v.)
# JAVA_TOOL_OPTIONS để JVM scale theo memory limit trong container.
ENV JAVA_OPTS="" \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]