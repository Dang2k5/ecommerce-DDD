# syntax=docker/dockerfile:1.7

# =========================
# 1) BUILD STAGE
# =========================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy các file cần thiết để tận dụng cache dependencies
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw

# Tải dependencies trước (tối ưu cache cho CI)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Dmaven.test.skip=true dependency:go-offline

# Copy toàn bộ source sau cùng để tránh mất cache khi code đổi
COPY . .

# Build jar
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -Dmaven.test.skip=true clean package


# =========================
# 2) RUNTIME STAGE
# =========================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Best-practice: chạy non-root
RUN useradd -r -u 10001 -g root -s /sbin/nologin appuser

# Tùy chọn: timezone VN/Thailand
ENV TZ=Asia/Bangkok
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25 -Duser.timezone=Asia/Bangkok"

# Copy jar từ build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Set quyền đọc/chạy cho user non-root
RUN chown -R 10001:0 /app && chmod -R g=u /app

USER 10001

# App chạy port 8083 (nhưng K8s vẫn sẽ set SERVER_PORT)
EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/app.jar"]